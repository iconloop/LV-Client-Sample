# crypto-research

###기능
+ Paillier 동형암호
+ 타원곡선 Point, Scalar 연산
+ Secure MPC ECDSA(Gennaro 20)
+ Range Proof

###Structure
+ iconloop.lab.crypto
  - common
    - Cryptographic Utilities
  - ec.bouncycastle.curve
    - 타원곡선에서의 연산(Point, Scalar) 구현
    - Bouncycastle JCE 사용
  - he.paillier
    - Paillier 동형 함수 구현
  - mpc.ecdsa
    - Multi Party Computation ECDSA 구현
    - PlayerKey : 서명에 참여하는 Player의 키(Shared Key) 관리(생성, 업데이트)
    - Signer : PlayerKey를 사용한 서명 연산 구현 
    - MPCClient : 키 분배, 서명 주체
    - MPCKeySharingPlayer : PlayerKey의 주체, PlayerKey의 관리(생성, 업데이트), 다른 Player와 데이터 송수신 주체
    - MPCSingingPlayer : PlayerKey의 주체, 서명 프로세스에 참여현, 다른 Player와 데이터 송수신 주체
    - MPCMessage : Player간 송수신 데이터 규격
    - MPCRepository : Player간 데이터 공유를 위한 저장소
  - securesharing
    - Secure Shaing을 위한 연산(Lagrangian Coefficient)
  - bulletproof
    - Bullet Proof 구현 
  - rangeproof
    - Range Proof 구현(단일 범위)
    - Multi-Range Proof 구현(복수 범위, 단일 Proof)

###Test
+ iconloop.lab.crypto.mpc.ecdsa
  - MPCKeySharingTest
    - 키 분배 및 검증
  - MPCKeyUpdateTest
    - 키 분배 및 업데이트 검증
  - MPCSingingTest
    - 키 분배 및 전자서명 생성/검증
  - SequenceTest
    - 임의의 N, T에 대하여 가능한 조합을 구성하여 키 분배, 업데이트, 전자서명 생성/검증 
+ iconloop.lab.crypto.rangeproof
  - RangeProofTest
    - 특정 secret에 대한 단일 범위 증명( a<= s < b)
  - MultiRangeProofTest
    - 두 개 secret에 대한 복수개의 범위 증명( a <= s1 < b 이고 c <= s2 < d)
    
### 참고
+ Secure MPC ECDSA
  - https://docs.google.com/document/d/1xdcgvRU-GWP29fddARGNICwQ7-__ORJF-WtjlhR9ZP4
+ Rang Proof
  - https://docs.google.com/document/d/1gCd1JvgYUOGP-TfeNttXMvzg5yXinjxJdsoWosEyqnM
