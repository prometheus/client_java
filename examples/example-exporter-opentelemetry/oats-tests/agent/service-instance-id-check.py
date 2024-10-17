#!/usr/bin/env python3

# This script is used to check if the service instance id is present in the exported data
# The script will return 0 if the service instance id is present in the exported data

from urllib.request import urlopen
import urllib.parse
import json

url = ' http://localhost:9090/api/v1/label/instance/values'
res = json.loads(urlopen(url).read().decode('utf-8'))

values = list(res['data'])
print(values)

if "localhost:8888" in values:
    values.remove("localhost:8888")

# both the agent and the exporter should report the same instance id
assert len(values) == 1

path = 'target_info{instance="%s"}' % values[0]
path = urllib.parse.quote_plus(path)
url = 'http://localhost:9090/api/v1/query?query=%s' % path
res = json.loads(urlopen(url).read().decode('utf-8'))

infos = res['data']['result']
print(infos)

# they should not have the same target info
# e.g. only the agent has telemetry_distro_name
assert len(infos) == 2

