import crypto from 'crypto';

const NCP_ACCESS_KEY = process.env.NCP_ACCESS_KEY || '';
const NCP_SECRET_KEY = process.env.NCP_SECRET_KEY || '';
const NCP_SERVICE_ID = process.env.NCP_SMS_SERVICE_ID || '';
const NCP_SENDER = process.env.NCP_SMS_SENDER || '';

function makeSignature(method: string, url: string, timestamp: string): string {
  const message = `${method} ${url}\n${timestamp}\n${NCP_ACCESS_KEY}`;
  return crypto
    .createHmac('sha256', NCP_SECRET_KEY)
    .update(message)
    .digest('base64');
}

export async function sendSMS(to: string, content: string): Promise<boolean> {
  if (!NCP_ACCESS_KEY || !NCP_SECRET_KEY || !NCP_SERVICE_ID || !NCP_SENDER) {
    console.log(`[DEV] SMS to ${to}: ${content}`);
    return true;
  }

  const timestamp = Date.now().toString();
  const uri = `/sms/v2/services/${encodeURIComponent(NCP_SERVICE_ID)}/messages`;
  const signature = makeSignature('POST', uri, timestamp);

  const body = {
    type: 'SMS',
    from: NCP_SENDER,
    content,
    messages: [{ to: to.replace(/-/g, '') }],
  };

  const res = await fetch(`https://sens.apigw.ntruss.com${uri}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'x-ncp-apigw-timestamp': timestamp,
      'x-ncp-iam-access-key': NCP_ACCESS_KEY,
      'x-ncp-apigw-signature-v2': signature,
    },
    body: JSON.stringify(body),
  });

  return res.ok;
}

// 인증코드 임시 저장 (메모리, 5분 TTL)
const codeStore = new Map<string, { code: string; expiresAt: number }>();

export function generateCode(): string {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

export function saveCode(phone: string, code: string) {
  const key = phone.replace(/-/g, '');
  codeStore.set(key, { code, expiresAt: Date.now() + 5 * 60 * 1000 });
}

export function verifyCode(phone: string, code: string): boolean {
  const key = phone.replace(/-/g, '');
  const entry = codeStore.get(key);
  if (!entry) return false;
  if (Date.now() > entry.expiresAt) {
    codeStore.delete(key);
    return false;
  }
  if (entry.code !== code) return false;
  codeStore.delete(key);
  return true;
}
