import json
import logging
from typing import List

from .helper import read_input_file, read_clue_file
from .interfaces import Manager, Storage
from .types import Commands

logging.basicConfig(level=logging.INFO)


class Handler:
    def __init__(self):
        self.managers = {}
        self.storages = {}

    def get_manager(self, endpoint):
        if endpoint in self.managers:
            return self.managers[endpoint]
        self.managers[endpoint] = Manager(endpoint)
        return self.managers[endpoint]

    def get_storage(self, storage_info):
        if storage_info['target'] in self.storages:
            return self.storages[storage_info['target']]
        self.storages[storage_info['target']] = Storage(storage_info)
        return self.storages[storage_info['target']]

    def _handle_get_vp(self, args):
        output_path = args.output

        manager = self.get_manager(endpoint=args.endpoint)
        logging.debug("- Requesting VPR...")
        vpr: dict = manager.request_vpr()  # TODO: THIS should contain VPRequest.
        logging.debug(f"- VPR Response: {vpr}")
        with open(output_path, "w") as f:
            f.write(json.dumps(vpr, indent=4))

    def _handle_get_storages(self, args):
        output_path = args.output

        manager = self.get_manager(endpoint=args.endpoint)
        logging.debug("- Requesting VID...")
        vp = None  # TODO: Make VP by VPR from lv-manager.
        vid_response = manager.issue_vid_request(vp)  # TODO: Fill VID Request according to responded VPR!
        logging.debug(f"- VID Response: {vid_response}")

        with open(output_path, "w") as f:
            f.write(json.dumps(vid_response, indent=4))

    def _handle_token(self, args):
        vid_response_msg: dict = read_input_file(args.input)
        storages: List[dict] = []
        vID = vid_response_msg["vID"]

        for storage_info in vid_response_msg["storages"]:
            storage = self.get_storage(storage_info)
            storage.token_request(vID)
            storages.append(storage.to_json())

        with open(args.output, "w") as f:
            f.write(json.dumps({
                "vID": vid_response_msg["vID"],
                "recovery_key": vid_response_msg["recovery_key"],
                "storages": storages
            }, indent=4))

    def _handle_store(self, args):
        clues: List[str] = read_clue_file(args.clues)
        vid_response_msg: dict = read_input_file(args.input)
        storages: List[dict] = []

        for clue, storage_info in zip(clues, vid_response_msg["storages"]):
            storage = self.get_storage(storage_info)
            storage.store_request(vid_response_msg["vID"], clue)
            storages.append(storage.to_json())

        with open(args.output, "w") as f:
            f.write(json.dumps({
                "vID": vid_response_msg["vID"],
                "recovery_key": vid_response_msg["recovery_key"],
                "storages": storages
            }, indent=4))

    def _handle_read(self, args):
        vid_response_msg: dict = read_input_file(args.input)
        storages = (self.get_storage(storage_info) for storage_info in vid_response_msg["storages"])
        gathered_clues = []

        for storage in storages:
            response = storage.clue_request(vid_response_msg["vID"])
            gathered_clues.append(response["clue"])

        with open(args.output, "w") as f:
            f.writelines("\n".join(gathered_clues))

    def __call__(self, command, args):
        handlers = {
            Commands.VPR: self._handle_get_vp,
            Commands.VID: self._handle_get_storages,
            Commands.TOKEN: self._handle_token,
            Commands.STORE: self._handle_store,
            Commands.RESTORE: self._handle_read,
        }

        if args.debug:
            logging.getLogger().setLevel(logging.DEBUG)

        return handlers[command](args)
