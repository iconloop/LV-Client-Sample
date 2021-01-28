from locust import HttpUser, task, between

from lvtool.__main__ import main
from lvtool.interfaces import Manager


class LiteVaultClient(HttpUser):
    wait_time = between(1, 2.5)

    @task
    def vpr(self):
        """Get Storages

        :return:
        """
        test_argv = ['lv-tool', 'vpr', '-e', 'lv-manager.iconscare.com', '-o', 'storages.json']

        def _send(_self, jwe_token):
            # print(f'In send jwe_token({jwe_token})')
            response = self.client.post(f"http://lv-manager.iconscare.com/vault", headers={
                "Authorization": jwe_token
            }, name='VPR')

            return response.text[1:-1]  # remove quotes...

        Manager._send = _send

        main(test_argv)

    def on_start(self):
        pass
