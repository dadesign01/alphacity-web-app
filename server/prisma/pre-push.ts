import { createConnection } from 'net';

// .env에서 DATABASE_URL 파싱
import { readFileSync } from 'fs';
import { execSync } from 'child_process';

function getDbCredentials() {
  try {
    const env = readFileSync('.env', 'utf-8');
    const match = env.match(/DATABASE_URL\s*=\s*"?mysql:\/\/([^:]+):([^@]+)@([^:]+):(\d+)\/([^?"'\s]+)/);
    if (!match) throw new Error('DATABASE_URL not found');
    return { user: match[1], password: match[2], host: match[3], port: match[4], database: match[5] };
  } catch {
    return null;
  }
}

const creds = getDbCredentials();
if (creds) {
  const sql = "UPDATE places SET category='event' WHERE category NOT IN ('food','exhibition','seminar','event');";
  try {
    const result = execSync(
      `mysql -u "${creds.user}" -p"${creds.password}" -h "${creds.host}" -P "${creds.port}" "${creds.database}" -e "${sql}"`,
      { stdio: ['pipe', 'pipe', 'pipe'] }
    );
    console.log('Pre-push: old place categories updated');
  } catch (e: any) {
    console.log('Pre-push: skipped -', e.stderr?.toString().trim() || 'no changes needed');
  }
} else {
  console.log('Pre-push: skipped - could not parse DATABASE_URL');
}
