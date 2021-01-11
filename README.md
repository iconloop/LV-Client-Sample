# LV-Client-Sample

# Requirements
1. **gradle**
~~~
$ brew install gradle
~~~

2. **Java Version 1.8**


### Build
~~~
$ gradle build
~~~

### Test Client Run
~~~
$ cd build/libs
$ java -jar LV-Client-Sample.jar -m 0
~~~

### Command line Run

~~~
$ cd build/libs
$ java -jar LV-Client-Sample.jar -t "blahblash" -n 3 -th 2
$ cat clues.txt
~~~

## Test tool
Written in python, the tool helps you testing LiteVault System.
It provides below features:
- Get storage info: create authorization to Manager (BACKUP_REQUEST & ISSUE_VID_REQUEST)
- Get storage token: create authorization to Storages (TOKEN_REQUEST)
- Manage clues: store and restore clues (STORE_REQUEST & CLUE_REQUEST)

### Install the tool
`$ pip install -e .[dev]`

> need python 3.7.3 or above

### Before started
- Prepare clues by using [java cli tool](### Command line Run)
- Run Manager and Storages

### Getting started
- `$ lv-tool -h`
