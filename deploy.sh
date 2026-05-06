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
if [ "$RESET" = "1" ]; then
  echo "    (RESET=1) Dropping & recreating all tables..."
  npx prisma db push --force-reset --accept-data-loss
else
  npx prisma db push --accept-data-loss
fi

# Run seed only if explicitly requested via SEED=1 env var
if [ "$SEED" = "1" ]; then
  echo ">>> Seeding database (SEED=1)..."
  npm run seed
fi

echo ">>> Building..."
npm run build

echo ">>> Restarting PM2..."
pm2 restart alphacity || pm2 start npm --name "alphacity" -- start

echo ">>> Deploy complete!"
