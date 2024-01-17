## Release Manager Setup

These setup steps only need to be performed by each contributor once.

### Create and publish a GPG key

NOTE: Refer to the current steps and guides on the ASF website:

  - [GPG Keys](http://www.apache.org/dev/openpgp.html#generate-key)
  - [Release Signing](http://www.apache.org/dev/release-signing.html#web-of-trust)

1. Generate a key-pair with gpg, using default key kind ("RSA and RSA") and keys size (4096).

2. Add your public key to the [KEYS](https://github.com/apache/streams/blob/master/KEYS) file. 

        gpg --list-sigs <Real Name> && gpg --armor -- export <Real Name>

3. Submit your public key to a key server. 

4. Ask multiple (at least 3) current Apache committers to sign your public key.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
