## Instructions:

Step 0: Plug in ethernet to Rover router (OL_DEMO). Connect wireless connection to wi-fi.

Step 1: Configure BOB_API_KEY into your .env
```bash
cp services/.env.example services/.env
# edit services/.env → BOB_API_KEY=<your key from https://bob.ibm.com, API Keys section> | Use Inference as API Key scope.
```

Step 2:
Build Images (Requires Internet)
```bash
docker-compose -f services/docker-compose.yml up -d --build
```

Step 3:
```bash
docker-compose -f services/docker-compose.yml down
```

Step 4:

Navigate to http://localhost:3000
Generate commands with Bob in the UI.