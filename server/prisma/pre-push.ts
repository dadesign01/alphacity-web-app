import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  try {
    // places 테이블의 이전 카테고리 값을 새 enum에 맞게 변환
    const result = await prisma.$executeRawUnsafe(
      "UPDATE places SET category='event' WHERE category NOT IN ('food','exhibition','seminar','event')"
    );
    console.log(`Pre-push: updated ${result} rows with old category values`);
  } catch {
    console.log('Pre-push: skipped (table not found or no old data)');
  }
  await prisma.$disconnect();
}

main();
