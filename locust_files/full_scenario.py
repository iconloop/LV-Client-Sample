from locust import HttpUser, task, between

from lvtool.__main__ import main
from lvtool.interfaces import Manager, Storage


class LiteVaultClient(HttpUser):
    wait_time = between(1, 2.5)

    @task(1)
    def vpr(self):
        """Get VPR

        :return:
        """
        def _send(_self: Manager, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='VPR_0')

            return response.text[1:-1]  # remove quotes...

        Manager._send = _send

        main(['lv-tool', 'vpr', '-e', 'lv-manager.iconscare.com', '-o', 'vpr.json'])

    @task(1)
    def vid(self):
        """Get Storages

        :return:
        """
        def _send(_self: Manager, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='VPR_1')

            return response.text[1:-1]  # remove quotes...

        Manager._send = _send

        main(['lv-tool', 'vid', '-e', 'lv-manager.iconscare.com', '-f', 'vpr.json', '-o', 'storages.json'])

    @task(2)
    def token(self):
        """Get Tokens from Storages

        :return:
        """
        def _send(_self: Storage, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='TOKEN_0')
            assert response.status_code == 200
            return response.text[1:-1]  # remove quotes...

        Storage._send = _send

        main(['lv-tool', 'token', '-f', 'storages.json', '-o', 'tokens.json'])

    @task(6)
    def store(self):
        """Store clues to Storages

        :return:
        """
        def _send(_self: Storage, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='STORE_0')
            assert response.status_code == 200
            return response.text[1:-1]  # remove quotes...

        Storage._send = _send

        main(['lv-tool', 'store', 'clues', '-f', 'tokens.json', '-o', 'store_output.json'])

    @task(3)
    def read(self):
        """Read clues from Storages

        :return:
        """
        def _send(_self: Storage, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"{_self._endpoint}/vault", headers={
                "Authorization": jwe_token
            }, name='CLUE_0')
            assert response.status_code == 200
            return response.text[1:-1]  # remove quotes...

        Storage._send = _send

        main(['lv-tool', 'read', '-f', 'store_output.json', '-o', 'restored_clues.txt'])

    def on_start(self):
        """Prepare files
        """
        main(['lv-tool', 'vpr', '-e', 'lv-manager.iconscare.com', '-o', 'vpr.json'])
        main(['lv-tool', 'vid', '-e', 'lv-manager.iconscare.com', '-f', 'vpr.json', '-o', 'storages.json'])
        main(['lv-tool', 'token', '-f', 'storages.json', '-o', 'tokens.json'])
        main(['lv-tool', 'store', 'clues', '-f', 'tokens.json', '-o', 'store_output.json'])
        main(['lv-tool', 'read', '-f', 'store_output.json', '-o', 'restored_clues.txt'])
