'use client';

import { Save } from 'lucide-react';
import { useEffect, useState } from 'react';

interface SettingsData {
  program: {
    name: string;
    description: string | null;
    startDate: string;
    endDate: string;
    autoNotification: boolean;
    collectStats: boolean;
  } | null;
  admin: {
    name: string;
    email: string;
    phone: string | null;
  } | null;
}

export default function SettingsPage() {
  const [settings, setSettings] = useState<SettingsData | null>(null);
  const [programForm, setProgramForm] = useState({ name: '', description: '', startDate: '', endDate: '', autoNotification: true, collectStats: true });
  const [adminForm, setAdminForm] = useState({ name: '', phone: '' });

  useEffect(() => {
    fetch('/api/v1/admin/settings').then((r) => r.json()).then((d) => {
      if (d.success) {
        setSettings(d.data);
        if (d.data.program) {
          setProgramForm({
            name: d.data.program.name,
            description: d.data.program.description || '',
            startDate: d.data.program.startDate?.split('T')[0] || '',
            endDate: d.data.program.endDate?.split('T')[0] || '',
            autoNotification: d.data.program.autoNotification,
            collectStats: d.data.program.collectStats,
          });
        }
        if (d.data.admin) {
          setAdminForm({ name: d.data.admin.name, phone: d.data.admin.phone || '' });
        }
      }
    });
  }, []);

  const handleSave = async () => {
    try {
      const res = await fetch('/api/v1/admin/settings', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ program: programForm, admin: adminForm }),
      });
      const data = await res.json();
      if (data.success) {
        alert('설정이 저장되었습니다');
      } else {
        alert(`설정 저장 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('설정 저장 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">설정</h2>
        <p className="text-sm text-gray-500 mt-1">시스템 설정 및 관리자 정보를 관리합니다</p>
      </div>

      <div className="space-y-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">기본 설정</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">프로그램명</label>
              <input type="text" value={programForm.name} onChange={(e) => setProgramForm({ ...programForm, name: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">개최 기간</label>
              <div className="grid grid-cols-2 gap-3">
                <input type="date" value={programForm.startDate} onChange={(e) => setProgramForm({ ...programForm, startDate: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                <input type="date" value={programForm.endDate} onChange={(e) => setProgramForm({ ...programForm, endDate: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">프로그램 소개</label>
              <textarea value={programForm.description} onChange={(e) => setProgramForm({ ...programForm, description: e.target.value })}
                rows={4}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">관리자 정보</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">이름</label>
              <input type="text" value={adminForm.name} onChange={(e) => setAdminForm({ ...adminForm, name: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">이메일</label>
              <input type="email" value={settings?.admin?.email || ''} disabled
                className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">연락처</label>
              <input type="tel" value={adminForm.phone} onChange={(e) => setAdminForm({ ...adminForm, phone: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">시스템 설정</h3>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div>
                <p className="text-sm font-medium text-gray-900">자동 알림 전송</p>
                <p className="text-xs text-gray-500">중요 이벤트 발생 시 자동으로 알림 전송</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" checked={programForm.autoNotification}
                  onChange={(e) => setProgramForm({ ...programForm, autoNotification: e.target.checked })}
                  className="sr-only peer" />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div>
                <p className="text-sm font-medium text-gray-900">사용자 통계 수집</p>
                <p className="text-xs text-gray-500">사용자 활동 데이터 수집 및 분석</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" checked={programForm.collectStats}
                  onChange={(e) => setProgramForm({ ...programForm, collectStats: e.target.checked })}
                  className="sr-only peer" />
                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button onClick={handleSave}
            className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            <Save className="w-4 h-4" />
            설정 저장
          </button>
        </div>
      </div>
    </div>
  );
}
