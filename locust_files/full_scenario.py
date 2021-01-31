from assam.jwt import encrypt_jwe, decrypt_jwe_with_cek
from locust import HttpUser, task, between

from lvtool.handlers import Handler
from lvtool.interfaces import Manager, Storage
from lvtool.parsers import init_parsers


class LiteVaultClient(HttpUser):
    wait_time = between(1, 2.5)

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.vpr_handler = Handler()
        self.vid_handler = Handler()
        self.token_handler = Handler()
        self.store_handler = Handler()
        self.read_handler = Handler()
        self.parser = init_parsers()

    @task(1)
    def vpr(self):
        """Get VPR

        :return:
        """
        self.vpr_handler(
            'vpr',
            self.parser.parse_args(['vpr', '-e', 'lv-manager.iconscare.com', '-o', 'vpr.json']))

    @task(1)
    def vid(self):
        """Get Storages

        :return:
        """
        self.vid_handler(
            'vid',
            self.parser.parse_args(['vid', '-e', 'lv-manager.iconscare.com', '-f', 'vpr.json', '-o', 'storages.json']))

    @task(2)
    def token(self):
        """Get Tokens from Storages

        :return:
        """
        self.token_handler(
            'token',
            self.parser.parse_args(['token', '-f', 'storages.json', '-o', 'tokens.json']))

    @task(6)
    def store(self):
        """Store clues to Storages

        :return:
        """
        self.store_handler(
            'store',
            self.parser.parse_args(['store', 'clues', '-f', 'tokens.json', '-o', 'store_output.json']))

    @task(3)
    def read(self):
        """Read clues from Storages

        :return:
        """
        self.read_handler(
            'read',
            self.parser.parse_args(['read', '-f', 'store_output.json', '-o', 'restored_clues.txt']))

    def on_start(self):
        """Prepare files
        """

        self.vpr_handler(
            'vpr',
            self.parser.parse_args(['vpr', '-e', 'lv-manager.iconscare.com', '-o', 'vpr.json']))
        self.vid_handler(
            'vid',
            self.parser.parse_args(['vid', '-e', 'lv-manager.iconscare.com', '-f', 'vpr.json', '-o', 'storages.json']))
        self.token_handler(
            'token',
            self.parser.parse_args(['token', '-f', 'storages.json', '-o', 'tokens.json']))
        self.store_handler(
            'store',
            self.parser.parse_args(['store', 'clues', '-f', 'tokens.json', '-o', 'store_output.json']))
        self.read_handler(
            'read',
            self.parser.parse_args(['read', '-f', 'store_output.json', '-o', 'restored_clues.txt']))

        # patch http client of handlers.
        def vpr_send(_self: Manager, jwe_token, cek):
            # print(f'In send jwe_token({jwe_token})')
            with self.client.post(f"{_self._endpoint}/vault",
                                  headers={"Authorization": jwe_token},
                                  name='VPR_0',
                                  catch_response=True) as _response:
                response = _response.text[1:-1]  # remove quotes....
                header, decrypt_response = decrypt_jwe_with_cek(response, cek)

                # print(f'decrypt_response({decrypt_response})')
                if decrypt_response['vp_request'] == {}:
                    _response.failure('There is no VP!')

            return response

        def vpr_request(_self: Manager) -> dict:
            payload = {
                "type": "BACKUP_REQUEST",
                "iat": 1111,
                "did": "issuer did of phone auth"
            }
            jwe_token, cek = encrypt_jwe(_self._key, payload)
            tokenized_response = _self._send(jwe_token, cek)
            header, backup_response = decrypt_jwe_with_cek(tokenized_response, cek)

            return backup_response

        for manager in self.vpr_handler.managers.values():
            manager._send = vpr_send.__get__(manager, Manager)
            manager.request_vpr = vpr_request.__get__(manager, Manager)

        def vid_send(_self: Manager, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='VPR_1')

            return response.text[1:-1]  # remove quotes...

        for manager in self.vid_handler.managers.values():
            manager._send = vid_send.__get__(manager, Manager)

        def token_send(_self: Storage, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='TOKEN_0')
            assert response.status_code == 200
            return response.text[1:-1]  # remove quotes...

        for storage in self.token_handler.storages.values():
            storage._send = token_send.__get__(storage, Storage)

        def store_send(_self: Storage, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='STORE_0')
            assert response.status_code == 200
            return response.text[1:-1]  # remove quotes...

        for storage in self.store_handler.storages.values():
            storage._send = store_send.__get__(storage, Storage)

        def read_send(_self, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='CLUE_0')
            assert response.status_code == 200
            return response.text[1:-1]  # remove quotes...

        for storage in self.read_handler.storages.values():
            storage._send = read_send.__get__(storage, Storage)
