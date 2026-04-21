import { prisma } from '@/lib/prisma';
import { notFound } from 'next/navigation';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: '이용약관 | 알파스탬프',
};

export const dynamic = 'force-dynamic';

export default async function TermsPage() {
  const term = await prisma.term.findFirst({
    where: { type: 'service', isActive: true },
    orderBy: { updatedAt: 'desc' },
  });

  if (!term) notFound();

  return (
    <main className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-3xl mx-auto bg-white rounded-lg border border-gray-200 p-6 sm:p-10">
        <header className="mb-8 pb-6 border-b border-gray-200">
          <h1 className="text-2xl sm:text-3xl font-semibold text-gray-900">
            {term.title}
          </h1>
          <p className="mt-2 text-sm text-gray-500">
            버전 v{term.version} · 최종 수정일{' '}
            {new Date(term.updatedAt).toLocaleDateString('ko-KR')}
          </p>
        </header>
        <article className="whitespace-pre-wrap break-words text-[15px] leading-7 text-gray-800">
          {term.content}
        </article>
      </div>
    </main>
  );
}
