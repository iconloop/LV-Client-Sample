from typing import Optional

import requests
from assam.jwt import encrypt_jwe, decrypt_jwe_with_cek, encrypt_jwe_with_cek
from jwcrypto import jwk

from .consts import manager_secp256k1_pubkey


class Manager:
    def __init__(self, endpoint: str):
        self._endpoint: str = endpoint if "http://" in endpoint else f"http://{endpoint}"
        self._key: jwk.JWK = jwk.JWK.from_json(manager_secp256k1_pubkey)

    def _send(self, jwe_token):
        response = requests.post(f"{self._endpoint}/vault", headers={
            "Authorization": jwe_token
        })
        return response.text[1:-1]  # remove quotes...

    def request_vpr(self) -> dict:
        payload = {
            "type": "BACKUP_REQUEST",
            "iat": 1111,
            "did": "issuer did of phone auth"
        }
        jwe_token, cek = encrypt_jwe(self._key, payload)
        tokenized_response = self._send(jwe_token)
        header, backup_response = decrypt_jwe_with_cek(tokenized_response, cek)

        return backup_response

    def issue_vid_request(self):
        payload = {
            "type": "ISSUE_VID_REQUEST",
            "iat": 1606125053,
            "vp": {
                "@context": ["http://vc.zzeung.id/credentials/v1.json"],
                "id": "https://www.iconloop.com/vp/qnfdkqkd/123623",
                "type": ["PresentationResponse"],
                "fulfilledCriteria": {
                    "conditionId": "uuid-requisite-0000-1111-2222",
                    "verifiableCredential": "YXNzZGZhc2Zkc2ZkYXNhZmRzYWtsc2Fkamtsc2FsJ3NhZGxrO3N….",
                    "verifiableCredentialParam": {
                        "@context": [
                            "http://vc.zzeung.id/credentials/v1.json",
                            "http://vc.zzeung.id/credentials/mobile_authentication/kor/v1.json"
                        ],
                        "type": ["CredentialParam", "MobileAuthenticationKorCredential"],
                        "credentialParam": {
                            "claim": {
                                "name": {
                                    "claimValue": "이제니",
                                    "salt": "d1341c4b0cbff6bee9118da10d6e85a5"
                                },
                                "gender": {
                                    "claimValue": "female",
                                    "salt": "d1341c4b0cbff6bee9118da10d6e85a5"
                                },
                                "telco": {
                                    "claimValue": "SKT",
                                    "salt": "345341c4b0cbff6bee9118da10d6e85a5"
                                },
                                "phoneNumber": {
                                    "claimValue": "01012345678",
                                    "salt": "a1341c4b0cbff6bee9118da10d6e85a5"
                                },
                                "connectionInformation": {
                                    "claimValue": "E21AEID0W6",
                                    "salt": "b1341c4b0cbff6bee9118da10d6e85a5"
                                },
                                "birthDate": {
                                    "claimValue": "1985-02-28",
                                    "salt": "af341c4b0cbff6bee9118da10d6e85a5"
                                },
                            },
                            "proofType": "hash",
                            "hashAlgorithm": "SHA-256"
                        }
                    }
                }
            }
        }

        jwe_token, cek = encrypt_jwe(self._key, payload)
        tokenized_response = self._send(jwe_token)
        header, issue_vid_response = decrypt_jwe_with_cek(tokenized_response, cek)

        return issue_vid_response


class Storage:
    def __init__(self, storage_info: dict):
        endpoint = storage_info['target']
        self._endpoint: str = endpoint if "http://" in endpoint else f"http://{endpoint}"
        self._key: jwk.JWK = jwk.JWK(**storage_info["key"])

        # ---- Optional
        self._token: Optional[str] = storage_info.get("token")
        cek = storage_info.get("cek")
        self._cek: Optional[jwk.JWK] = jwk.JWK(**cek) if cek else None
        self._vid: Optional[str] = storage_info.get("vid")
        self._tag: Optional[str] = storage_info.get("tag")

    def _send(self, jwe_token):
        response = requests.post(f"{self._endpoint}/vault", headers={
            "Authorization": jwe_token
        })
        assert response.status_code == 200
        return response.text[1:-1]  # remove quotes...

    def token_request(self) -> dict:
        payload = {
            "type": "TOKEN_REQUEST",
            "iat": 1111,
            "vp": {"dujmmy": "vp"}  # TODO:
        }
        jwe_token, cek = encrypt_jwe(self._key, payload)
        tokenized_response = self._send(jwe_token)
        header, token_response = decrypt_jwe_with_cek(tokenized_response, cek)

        self._cek = cek
        self._token = token_response["token"]

        return token_response

    def store_request(self, clue) -> dict:
        assert self._token
        assert self._cek

        payload = {
            "type": "STORE_REQUEST",
            "iat": 1111,
            "vID": "WriteMyClueByUsingThisAsAkey",
            "clue": clue
        }
        jwe_token = encrypt_jwe_with_cek(self._cek, payload, kid=self._token)
        tokenized_response = self._send(jwe_token)
        header, store_response = decrypt_jwe_with_cek(tokenized_response, self._cek)

        self._vid = store_response["vID"]
        self._tag = store_response["tag"]

        return store_response

    def clue_request(self) -> dict:
        assert self._token
        assert self._cek
        assert self._vid

        payload = {
            "type": "CLUE_REQUEST",
            "iat": 1111,
            "vID": self._vid
        }
        jwe_token = encrypt_jwe_with_cek(self._cek, payload, kid=self._token)
        tokenized_response = self._send(jwe_token)
        header, clue_response = decrypt_jwe_with_cek(tokenized_response, self._cek)

        return clue_response

    def to_json(self):
        obj = {
            "target": self._endpoint,
            "token": self._token,
            "key": self._key.export_public(as_dict=True),
            "cek": self._cek.export_symmetric(as_dict=True)
        }
        if self._vid:
            obj["vid"] = self._vid
        if self._tag:
            obj["tag"] = self._tag

        return obj
