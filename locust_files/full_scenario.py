from locust import User, task, between

from lvtool.__main__ import main


class LiteVaultClient(User):
    wait_time = between(1, 2.5)

    @task
    def vpr(self):
        """Get Storages

        :return:
        """
        test_argv = ['lv-tool', 'vpr', '-e', 'lv-manager.iconscare.com', '-o', 'storages.json']
        main(test_argv)

    @task(3)
    def view_items(self):
        pass

    def on_start(self):
        pass
