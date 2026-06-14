import urllib.request
import json
import urllib.error

data = json.dumps({'username':'testuser_new3','password':'password'}).encode()
req = urllib.request.Request('http://localhost:8000/api/auth/login', data=data, headers={'Content-Type': 'application/json'})

try:
    response = urllib.request.urlopen(req)
    print("SUCCESS")
    print(response.read().decode())
except urllib.error.HTTPError as e:
    print(f"HTTP {e.code}: {e.read().decode()}")
    print("Headers:", e.headers)
