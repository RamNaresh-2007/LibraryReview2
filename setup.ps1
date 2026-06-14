Write-Host "Installing Gateway Requirements..."
cd backend_gateway
pip install -r requirements.txt
cd ..

Write-Host "Installing Root NPM Dependencies..."
npm install

Write-Host "Installing Node Service Dependencies..."
cd node_service
npm install
cd ..

Write-Host "Installing Library Service Dependencies..."
cd library_service
npm install
cd ..

Write-Host "Installing Frontend Dependencies..."
cd frontend
npm install
cd ..

Write-Host "Compiling Spring Boot..."
cd backend_springboot
./mvnw clean compile
cd ..

Write-Host "Setup Complete! Run 'npm start' to boot up the entire ecosystem."
