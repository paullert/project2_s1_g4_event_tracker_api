# HTTP Tests Notes

## How it works
It uses jetbrains http client, install the plugin if you wish to use them.

## Setup
Create a file called `http-client.env.json`

put the following in there, changing token to bear token id (id token, not access token)
```
{
  "local": {
    "baseUrl": "http://localhost:8080",
    "token": "paste-a-valid-bearer-token-here"
  }
}
```

Be sure to run with the environment file. This is done by clicking `run with:` and select local

## Using
Click Plus sign on top right, while spring boot app is running.