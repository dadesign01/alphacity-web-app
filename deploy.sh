#!/bin/bash
# 서버에서 실행하는 배포 스크립트
# 사용법: bash /home/alphacity/alphacity/deploy.sh

cd /home/alphacity/alphacity

echo ">>> git pull..."
git pull origin main

echo ">>> Installing dependencies..."
cd server
npm install

echo ">>> Generating Prisma client..."
npx prisma generate

echo ">>> Syncing database schema..."
npx prisma db push

echo ">>> Building..."
npm run build

echo ">>> Restarting PM2..."
pm2 restart alphacity || pm2 start npm --name "alphacity" -- start

echo ">>> Deploy complete!"
