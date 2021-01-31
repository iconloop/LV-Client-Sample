import json
import secrets
from pathlib import Path
from unittest.mock import MagicMock

import pytest
from jwcrypto import jwk

from lvtool.handlers import Handler
from lvtool.parsers import init_parsers
from lvtool.types import Commands

storage0_key = {
    "crv": "P-256",
    "d": "l0uCMu6K24Yhi6O5CupXpTc1NSTDwLKAC74ggTeiLkE",
    "kty": "EC",
    "x": "mhbdCRKU2Gxd1ccy9-w67SYfjcm2EUtKQCLV0hFmUgQ",
    "y": "ipu-riOUjZPzF3ybrjO3TqnDyXqfsi35oXcQk-ORXIA",
}

storage1_key = {
    "crv": "P-256",
    "d": "n_ZHDppmd-g2ELUn5VLQq600IVGLfHwH0TGKrEIVT3k",
    "kty": "EC",
    "x": "hTwgAVlv6syYsVtxnLbimkrNp1YyUEHLvXU_Ciz8Tvo",
    "y": "-ifyo0CxSghFaDma0tSyAGOeTXoay3N64LcqYnFpAMY",
}

storage2_key = {
    "crv": "P-256",
    "d": "cHaeXEaTmGIj4UbZARnqCHScazt3QRrEsaEHIJztWug",
    "kty": "EC",
    "x": "Ac9Trq_bR4ciMiFZF1CVkRjsSRvCyA8OTT0-6uZJdTU",
    "y": "pcJyYIV2SrJIbEXoT0J6pFQlx1qIrxB9Oi74CYqmxbE",
}


class TestScenario:
    """Linear scenario test. Should be sequential."""

    MANAGER_ENDPOINT = "localhost:8000"
    DUMMY_CLUES = [
        "DS4R0ErvCdZiNvaltluPb6bJyiAMsYKr2N5OgSopAzg",
        "OTThVPWZDDnXJms0a-p6RvJnHkEM46qz37I2WkPAygQ",
        "GbXJSEnce8LybJVsQQl5F31qOjiNPDoeEc63R9trI8s",
    ]
    CLUE_PATH = "./clues"

    MANAGER_OUTPUT = "./output.json"
    TOKENS_OUTPUT = "./tokens.json"
    STORE_OUTPUT = "./stored.json"
    RESTORE_OUTPUT = "./clues_restored.txt"

    @pytest.fixture(autouse=True)
    def clear_input_files(self, tmp_path):
        # GIVEN There is no input files
        Path(self.MANAGER_OUTPUT).unlink(missing_ok=True)
        Path(self.TOKENS_OUTPUT).unlink(missing_ok=True)
        Path(self.STORE_OUTPUT).unlink(missing_ok=True)
        Path(self.RESTORE_OUTPUT).unlink(missing_ok=True)

        assert not Path(self.MANAGER_OUTPUT).exists()
        assert not Path(self.TOKENS_OUTPUT).exists()
        assert not Path(self.STORE_OUTPUT).exists()
        assert not Path(self.RESTORE_OUTPUT).exists()

    @pytest.fixture
    def mock_manager_server(self, monkeypatch):
        # GIVEN I mocked Manager Server
        from lvtool.handlers import Manager

        mock_return_request_vpr = {}  # TODO:
        monkeypatch.setattr(
            Manager, "request_vpr", MagicMock(return_value=mock_return_request_vpr)
        )

        mock_return_issue_vid_request = {
            "iat": 1610093682,
            "vID": "b6e54c39af450e26c52a68089fd45b109cff752dc2a87da02307efc7fd0370d9",
            "recovery_key": "1262c6920b5fe24d0c6e0945016c0bfc",
            "storages": [
                {
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "qkM1f-Tw3ZI8zdm0RbRhSvlEU_7ndDMHMRZYZaSwEik",
                        "y": "2qQu5G0KBgIdpKRJQnC-RIPjUoDRVDB-hf2xxh4sis0",
                    },
                    "target": "127.0.0.1:8100",
                },
                {
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "r0ElO-afaSSsivnStJC54wl69H0oChLpfhoAcwidAlo",
                        "y": "yvfO7qEAWmEW2dcADAmpjMG-6B7L74Y3qBsCNl0yjC8",
                    },
                    "target": "127.0.0.1:8101",
                },
                {
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "7dXJL08gYcO4C7MX-6KFxCeS8xlcsZzmWPOZ243M4m4",
                        "y": "l18Dk2r7gaQl_GNOisn28A1YhyZmnEIkJH-ZRXWmYjg",
                    },
                    "target": "127.0.0.1:8103",
                },
            ],
            "type": "ISSUE_VID_RESPONSE",
        }
        monkeypatch.setattr(
            Manager,
            "issue_vid_request",
            MagicMock(return_value=mock_return_issue_vid_request),
        )

    @pytest.fixture
    def mock_storage_server(self, monkeypatch):
        # GIVEN I mocked Storage Server
        from lvtool.handlers import Storage

        def mock_token_request(self):
            self._cek = jwk.JWK.generate(kty="oct")
            self._token = secrets.token_hex()

        def mock_store_request(self, vid, clue):
            self._tag = "0"

        def mock_clue_request(self, vid):
            if "8100" in self._endpoint:
                return {"clue": TestScenario.DUMMY_CLUES[0]}
            elif "8101" in self._endpoint:
                return {"clue": TestScenario.DUMMY_CLUES[1]}
            elif "8102" in self._endpoint:
                return {"clue": TestScenario.DUMMY_CLUES[2]}
            else:
                raise RuntimeError("What..?")

        monkeypatch.setattr(Storage, "token_request", mock_token_request)
        monkeypatch.setattr(Storage, "store_request", mock_store_request)
        monkeypatch.setattr(Storage, "clue_request", mock_clue_request)

    def _create_vid_response_file(self) -> str:
        """Create ISSUE_VID_RESPONSE file and return its path."""
        vid_response_msg = {
            "iat": 1609996961,
            "vID": "b6e54c39af450e26c52a68089fd45b109cff752dc2a87da02307efc7fd0370d9",
            "recovery_key": "931c4b9a00acb4b7a13add83490bddfa",
            "type": "ISSUE_VID_RESPONSE",
            "storages": [
                {"key": storage0_key.copy(), "target": "127.0.0.1:8100"},
                {"key": storage1_key.copy(), "target": "127.0.0.1:8101"},
                {"key": storage2_key.copy(), "target": "127.0.0.1:8102"},
            ],
        }
        for storage in vid_response_msg["storages"]:
            del storage["key"]["d"]

        with open(self.MANAGER_OUTPUT, "w") as f:
            f.write(json.dumps(vid_response_msg))

        return self.MANAGER_OUTPUT

    def _create_clue_file(self) -> str:
        """Create clue file and return its path."""
        with open(self.CLUE_PATH, "w") as f:
            f.writelines("\n".join(self.DUMMY_CLUES))

        return self.CLUE_PATH

    def _create_tokens_output(self):
        assert not Path(self.TOKENS_OUTPUT).exists()

        output = {
            "vID": "b6e54c39af450e26c52a68089fd45b109cff752dc2a87da02307efc7fd0370d9",
            "recovery_key": "931c4b9a00acb4b7a13add83490bddfa",
            "storages": [
                {
                    "target": "http://127.0.0.1:8100",
                    "token": "831dc3dc357045fc77cc8e68ec4d1b857774c512a188fc39d9d7d1ca07b27f1f",
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "mhbdCRKU2Gxd1ccy9-w67SYfjcm2EUtKQCLV0hFmUgQ",
                        "y": "ipu-riOUjZPzF3ybrjO3TqnDyXqfsi35oXcQk-ORXIA",
                    },
                    "cek": {"kty": "oct", "k": "AYpUpQpu0OMVUJXhq8DFNw"},
                },
                {
                    "target": "http://127.0.0.1:8101",
                    "token": "d67f839306cf7c9a05f8adc11dc26d9d89ae55cfd5c8ccddd3b4ca1d2f65aadc",
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "hTwgAVlv6syYsVtxnLbimkrNp1YyUEHLvXU_Ciz8Tvo",
                        "y": "-ifyo0CxSghFaDma0tSyAGOeTXoay3N64LcqYnFpAMY",
                    },
                    "cek": {"kty": "oct", "k": "jOZN4o-hrdJfWHGiIBGGNg"},
                },
                {
                    "target": "http://127.0.0.1:8102",
                    "token": "066145d88d6169bbd3d1d41edc472964a953f467d3a15ff9bcea223e1033f30b",
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "Ac9Trq_bR4ciMiFZF1CVkRjsSRvCyA8OTT0-6uZJdTU",
                        "y": "pcJyYIV2SrJIbEXoT0J6pFQlx1qIrxB9Oi74CYqmxbE",
                    },
                    "cek": {"kty": "oct", "k": "gGvj2YUVmOjOfO0xBfH5_g"},
                },
            ],
        }
        with open(self.TOKENS_OUTPUT, "w") as f:
            f.write(json.dumps(output, indent=4))

        assert Path(self.TOKENS_OUTPUT).exists()

    def _create_store_output(self):
        assert not Path(self.STORE_OUTPUT).exists()

        output = {
            "vID": "b6e54c39af450e26c52a68089fd45b109cff752dc2a87da02307efc7fd0370d9",
            "recovery_key": "931c4b9a00acb4b7a13add83490bddfa",
            "storages": [
                {
                    "target": "http://127.0.0.1:8100",
                    "token": "831dc3dc357045fc77cc8e68ec4d1b857774c512a188fc39d9d7d1ca07b27f1f",
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "mhbdCRKU2Gxd1ccy9-w67SYfjcm2EUtKQCLV0hFmUgQ",
                        "y": "ipu-riOUjZPzF3ybrjO3TqnDyXqfsi35oXcQk-ORXIA",
                    },
                    "cek": {"kty": "oct", "k": "AYpUpQpu0OMVUJXhq8DFNw"},
                    "tag": "0",
                },
                {
                    "target": "http://127.0.0.1:8101",
                    "token": "d67f839306cf7c9a05f8adc11dc26d9d89ae55cfd5c8ccddd3b4ca1d2f65aadc",
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "hTwgAVlv6syYsVtxnLbimkrNp1YyUEHLvXU_Ciz8Tvo",
                        "y": "-ifyo0CxSghFaDma0tSyAGOeTXoay3N64LcqYnFpAMY",
                    },
                    "cek": {"kty": "oct", "k": "jOZN4o-hrdJfWHGiIBGGNg"},
                    "tag": "0",
                },
                {
                    "target": "http://127.0.0.1:8102",
                    "token": "066145d88d6169bbd3d1d41edc472964a953f467d3a15ff9bcea223e1033f30b",
                    "key": {
                        "kty": "EC",
                        "crv": "P-256",
                        "x": "Ac9Trq_bR4ciMiFZF1CVkRjsSRvCyA8OTT0-6uZJdTU",
                        "y": "pcJyYIV2SrJIbEXoT0J6pFQlx1qIrxB9Oi74CYqmxbE",
                    },
                    "cek": {"kty": "oct", "k": "gGvj2YUVmOjOfO0xBfH5_g"},
                    "tag": "0",
                },
            ],
        }
        with open(self.STORE_OUTPUT, "w") as f:
            f.write(json.dumps(output, indent=4))

        assert Path(self.STORE_OUTPUT).exists()

    def test_auth_with_manager(self, mock_manager_server):
        # GIVEN I created CLI instructions
        parser = init_parsers()
        args = parser.parse_args([Commands.VPR, "-e", self.MANAGER_ENDPOINT, "-o", self.MANAGER_OUTPUT])

        # WHEN I call it
        Handler()(Commands.VPR, args)

        # THEN output file should exist
        assert Path(self.MANAGER_OUTPUT).exists()

    def test_auth_storage(self, mock_storage_server):
        # GIVEN I already finished authentication with Manager
        storages_file_path = self._create_vid_response_file()

        # AND I created CLI instructions
        parser = init_parsers()
        args = parser.parse_args(
            [Commands.TOKEN, "-f", storages_file_path, "-o", self.TOKENS_OUTPUT]
        )

        # WHEN I call it
        Handler()(Commands.TOKEN, args)

        # THEN output file should be exist
        assert Path(self.TOKENS_OUTPUT).exists()

    def test_store_clue(self, mock_storage_server):
        # GIVEN I already finished authentication with storages
        self._create_tokens_output()

        # AND I have clues
        clue_path = self._create_clue_file()

        # AND I created CLI instructions
        parser = init_parsers()
        args = parser.parse_args([Commands.STORE, clue_path, "-f", self.TOKENS_OUTPUT, "-o", self.STORE_OUTPUT])

        # WHEN I call it
        Handler()(Commands.STORE, args)

        # THEN output file should be exist
        assert Path(self.TOKENS_OUTPUT).exists()

    def test_request_clue(self, mock_storage_server):
        # GIVEN I already finished storing clues
        self._create_store_output()

        # AND I created CLI instructions
        parser = init_parsers()
        args = parser.parse_args(
            [Commands.RESTORE, "-f", self.STORE_OUTPUT, "-o", self.RESTORE_OUTPUT]
        )

        # WHEN I call it
        Handler()(Commands.RESTORE, args)

        # THEN output file should be exist
        assert Path(self.RESTORE_OUTPUT).exists()

    def test_clue_request_without_store_clues(self, mock_storage_server):
        # GIVEN I already finished authentication with storages
        self._create_tokens_output()

        # AND I created CLI instructions
        parser = init_parsers()
        args = parser.parse_args(
            [Commands.RESTORE, "-f", self.TOKENS_OUTPUT, "-o", self.RESTORE_OUTPUT]
        )

        # WHEN I call it
        Handler()(Commands.RESTORE, args)

        # THEN output file should be exist
        assert Path(self.RESTORE_OUTPUT).exists()
