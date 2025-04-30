#!/usr/bin/env python3

"""This script is used to check if the service instance id is present in the exported data
The script will return 0 if the service instance id is present in the exported data"""

import urllib.parse
from urllib.request import urlopen

URL = " http://localhost:9090/api/v1/query?query=target_info"
with urlopen(URL) as response:
  # read the response
  res = response.read()
  # decode the response
  res = res.decode("utf-8")

# uncomment the following line to use the local file instead of the url - for debugging
# with open('example_target_info.json') as f:
#   res = json.load(f)

values = list(
  {
    r["metric"]["instance"]
    for r in res["data"]["result"]
    if not r["metric"]["service_name"] == "otelcol-contrib"
  }
)
print(values)

# both the agent and the exporter should report the same instance id
assert len(values) == 1

path = f'target_info{{instance="{values[0]}"}}'
path = urllib.parse.quote_plus(path)
URL = f"http://localhost:9090/api/v1/query?query={path}"
with urlopen(URL) as response:
  # read the response
  res = response.read()
  # decode the response
  res = res.decode("utf-8")

infos = res["data"]["result"]
print(infos)

# they should not have the same target info
# e.g. only the agent has telemetry_distro_name
assert len(infos) == 2
