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

echo ">>> Migrating old data before schema push..."
DB_URL=$(grep DATABASE_URL .env | head -1 | cut -d= -f2- | tr -d '"' | tr -d "'")
if [ -n "$DB_URL" ]; then
  echo "UPDATE places SET category='event' WHERE category NOT IN ('food','exhibition','seminar','event');" | npx prisma db execute --stdin --url "$DB_URL" 2>/dev/null || true
fi

echo ">>> Syncing database schema..."
npx prisma db push --accept-data-loss

echo ">>> Seeding database..."
npm run seed

echo ">>> Building..."
npm run build

echo ">>> Restarting PM2..."
pm2 restart alphacity || pm2 start npm --name "alphacity" -- start

echo ">>> Deploy complete!"
