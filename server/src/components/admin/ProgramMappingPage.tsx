'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  MapPin,
  Target,
  Stamp,
  Search,
  Plus,
  X,
  GripVertical,
  Save,
  HelpCircle,
  Clock,
  Upload,
  Trash2,
  ChevronDown,
  ChevronUp,
  Check,
  Loader2,
} from 'lucide-react';

type TabType = 'places' | 'missions' | 'stamps';

interface Program {
  id: number;
  name: string;
  description: string | null;
  category: string;
  startDate: string;
  endDate: string;
  status: string;
}

interface Place {
  id: number;
  name: string;
  category: string;
  address: string | null;
  latitude: number;
  longitude: number;
  sortOrder?: number;
}

interface Mission {
  id: number;
  name: string;
  type: string;
  placeId: number | null;
  programId: number | null;
  question: string | null;
  answer: string | null;
  options: string | null;
  stayMinutes: number | null;
  place: { id: number; name: string } | null;
}

interface StampData {
  id: number;
  name: string;
  conditionType: string;
  conditionDetail: string | null;
  imageUrl: string | null;
  programId: number | null;
  placeId: number | null;
  place: { id: number; name: string } | null;
}

interface MissionDraft {
  tempId: string;
  name: string;
  type: string;
  placeId: number | null;
  question: string;
  answer: string;
  stayMinutes: number | null;
}

interface StampDraft {
  tempId: string;
  name: string;
  conditionType: string;
  conditionDetail: string;
  placeId: number | null;
}

const categoryLabels: Record<string, string> = {
  entrance: '입구',
  food: '음식점',
  facility: '편의시설',
  photo_zone: '포토존',
  other: '기타',
};

const statusLabels: Record<string, string> = {
  scheduled: '예정',
  in_progress: '진행중',
  ended: '종료',
};

const programCategoryLabels: Record<string, string> = {
  exhibition: '전시',
  seminar: '세미나',
  food: '음식',
  event: '이벤트',
};

const missionTypeLabels: Record<string, string> = {
  quiz: '퀴즈',
  location_auth: '위치 인증',
  stay_time: '체류 시간',
};

const conditionLabels: Record<string, string> = {
  mission_complete: '미션 완료',
  event_participate: '이벤트 참여',
  place_visit: '장소 방문',
  quiz_correct: '퀴즈 정답',
};

const missionTemplates = [
  { type: 'location_auth', name: '위치 인증', icon: MapPin },
  { type: 'quiz', name: '퀴즈', icon: HelpCircle },
  { type: 'stay_time', name: '체류 시간', icon: Clock },
];

export default function ProgramMappingPage() {
  // Program selection
  const [programs, setPrograms] = useState<Program[]>([]);
  const [selectedProgramId, setSelectedProgramId] = useState<number | null>(null);

  // Tab
  const [activeTab, setActiveTab] = useState<TabType>('places');

  // Places tab
  const [allPlaces, setAllPlaces] = useState<Place[]>([]);
  const [connectedPlaceIds, setConnectedPlaceIds] = useState<number[]>([]);
  const [placeSearchTerm, setPlaceSearchTerm] = useState('');
  const [placeCategoryFilter, setPlaceCategoryFilter] = useState('');

  // Missions tab
  const [connectedMissions, setConnectedMissions] = useState<Mission[]>([]);
  const [newMissions, setNewMissions] = useState<MissionDraft[]>([]);
  const [removedMissionIds, setRemovedMissionIds] = useState<number[]>([]);

  // Stamps tab
  const [connectedStamps, setConnectedStamps] = useState<StampData[]>([]);
  const [newStamps, setNewStamps] = useState<StampDraft[]>([]);
  const [removedStampIds, setRemovedStampIds] = useState<number[]>([]);
  const [showStampLibrary, setShowStampLibrary] = useState(false);
  const [allStamps, setAllStamps] = useState<StampData[]>([]);
  const [librarySearchTerm, setLibrarySearchTerm] = useState('');
  const [selectedLibraryStampIds, setSelectedLibraryStampIds] = useState<number[]>([]);
  const [showStampDropdown, setShowStampDropdown] = useState(false);

  // UI state
  const [isSaving, setIsSaving] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Fetch programs on mount
  useEffect(() => {
    fetch('/api/v1/admin/programs')
      .then(r => r.json())
      .then(data => {
        if (data.success) setPrograms(data.data);
      });
  }, []);

  // Fetch mapping data when program selected
  const fetchMappingData = useCallback(async (programId: number) => {
    setIsLoading(true);
    try {
      const [mappingRes, availableRes] = await Promise.all([
        fetch(`/api/v1/admin/program-mapping/${programId}`).then(r => r.json()),
        fetch('/api/v1/admin/program-mapping/available').then(r => r.json()),
      ]);

      if (mappingRes.success) {
        setConnectedPlaceIds(mappingRes.data.places.map((p: Place) => p.id));
        setConnectedMissions(mappingRes.data.missions);
        setConnectedStamps(mappingRes.data.stamps);
      }

      if (availableRes.success) {
        setAllPlaces(availableRes.data.places);
        setAllStamps(availableRes.data.stamps);
      }

      setNewMissions([]);
      setNewStamps([]);
      setRemovedMissionIds([]);
      setRemovedStampIds([]);
    } catch {
      alert('데이터를 불러오는데 실패했습니다');
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    if (selectedProgramId) {
      fetchMappingData(selectedProgramId);
    }
  }, [selectedProgramId, fetchMappingData]);

  const selectedProgram = programs.find(p => p.id === selectedProgramId);

  // Places handlers
  const handleAddPlace = (placeId: number) => {
    if (!connectedPlaceIds.includes(placeId)) {
      setConnectedPlaceIds([...connectedPlaceIds, placeId]);
    }
  };

  const handleRemovePlace = (placeId: number) => {
    setConnectedPlaceIds(connectedPlaceIds.filter(id => id !== placeId));
  };

  const handleMovePlaceUp = (index: number) => {
    if (index === 0) return;
    const ids = [...connectedPlaceIds];
    [ids[index - 1], ids[index]] = [ids[index], ids[index - 1]];
    setConnectedPlaceIds(ids);
  };

  const handleMovePlaceDown = (index: number) => {
    if (index === connectedPlaceIds.length - 1) return;
    const ids = [...connectedPlaceIds];
    [ids[index], ids[index + 1]] = [ids[index + 1], ids[index]];
    setConnectedPlaceIds(ids);
  };

  // Mission handlers
  const handleAddMission = (type: string) => {
    const template = missionTemplates.find(t => t.type === type);
    setNewMissions([...newMissions, {
      tempId: `new-${Date.now()}`,
      name: `새 ${template?.name} 미션`,
      type,
      placeId: null,
      question: '',
      answer: '',
      stayMinutes: type === 'stay_time' ? 5 : null,
    }]);
  };

  const handleRemoveExistingMission = (missionId: number) => {
    setConnectedMissions(connectedMissions.filter(m => m.id !== missionId));
    setRemovedMissionIds([...removedMissionIds, missionId]);
  };

  const handleRemoveNewMission = (tempId: string) => {
    setNewMissions(newMissions.filter(m => m.tempId !== tempId));
  };

  const handleUpdateNewMission = (tempId: string, updates: Partial<MissionDraft>) => {
    setNewMissions(newMissions.map(m => m.tempId === tempId ? { ...m, ...updates } : m));
  };

  // Stamp handlers
  const handleAddStamp = () => {
    setNewStamps([...newStamps, {
      tempId: `new-${Date.now()}`,
      name: '새 스탬프',
      conditionType: 'mission_complete',
      conditionDetail: '',
      placeId: null,
    }]);
  };

  const handleRemoveExistingStamp = (stampId: number) => {
    setConnectedStamps(connectedStamps.filter(s => s.id !== stampId));
    setRemovedStampIds([...removedStampIds, stampId]);
  };

  const handleRemoveNewStamp = (tempId: string) => {
    setNewStamps(newStamps.filter(s => s.tempId !== tempId));
  };

  const handleUpdateNewStamp = (tempId: string, updates: Partial<StampDraft>) => {
    setNewStamps(newStamps.map(s => s.tempId === tempId ? { ...s, ...updates } : s));
  };

  const handleAddLibraryStamps = () => {
    const stampsToLink = allStamps.filter(s => selectedLibraryStampIds.includes(s.id));
    setConnectedStamps([...connectedStamps, ...stampsToLink]);
    setSelectedLibraryStampIds([]);
    setShowStampLibrary(false);
  };

  // Save
  const handleSave = async () => {
    if (!selectedProgramId) return;
    setIsSaving(true);

    try {
      const res = await fetch(`/api/v1/admin/program-mapping/${selectedProgramId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          places: {
            placeIds: connectedPlaceIds,
          },
          missions: {
            unlink: removedMissionIds,
            link: connectedMissions.map(m => m.id),
            create: newMissions.map(m => ({
              name: m.name,
              type: m.type,
              placeId: m.placeId,
              question: m.question || null,
              answer: m.answer || null,
              stayMinutes: m.stayMinutes,
            })),
          },
          stamps: {
            unlink: removedStampIds,
            link: connectedStamps.map(s => s.id),
            create: newStamps.map(s => ({
              name: s.name,
              conditionType: s.conditionType,
              conditionDetail: s.conditionDetail || null,
              placeId: s.placeId,
            })),
          },
        }),
      });

      const data = await res.json();
      if (data.success) {
        alert('저장되었습니다');
        fetchMappingData(selectedProgramId);
      } else {
        alert(data.error?.message || '저장에 실패했습니다');
      }
    } catch {
      alert('저장에 실패했습니다');
    }
    setIsSaving(false);
  };

  // Filtered places for left panel
  const filteredPlaces = allPlaces.filter(place => {
    if (placeSearchTerm && !place.name.toLowerCase().includes(placeSearchTerm.toLowerCase())) return false;
    if (placeCategoryFilter && place.category !== placeCategoryFilter) return false;
    return true;
  });

  // Library stamps (exclude already connected)
  const connectedStampIds = new Set(connectedStamps.map(s => s.id));
  const libraryStamps = allStamps.filter(s => {
    if (connectedStampIds.has(s.id)) return false;
    if (librarySearchTerm && !s.name.toLowerCase().includes(librarySearchTerm.toLowerCase())) return false;
    return true;
  });

  const tabs = [
    { id: 'places' as TabType, label: '장소 연결', icon: MapPin },
    { id: 'missions' as TabType, label: '미션 연결', icon: Target },
    { id: 'stamps' as TabType, label: '스탬프 설정', icon: Stamp },
  ];

  // === Render: Places Tab ===
  const renderPlacesTab = () => (
    <div className="grid grid-cols-2 gap-6">
      {/* Left Panel - Available Places */}
      <div className="bg-white border border-gray-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">등록된 장소</h4>
        <div className="relative mb-3">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="장소 검색..."
            value={placeSearchTerm}
            onChange={e => setPlaceSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <select
          value={placeCategoryFilter}
          onChange={e => setPlaceCategoryFilter(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
        >
          <option value="">전체 유형</option>
          {Object.entries(categoryLabels).map(([key, label]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>

        <div className="space-y-2 max-h-96 overflow-y-auto">
          {filteredPlaces.map(place => (
            <div key={place.id} className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">{place.name}</p>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 rounded">
                    {categoryLabels[place.category] || place.category}
                  </span>
                  {place.address && <p className="text-xs text-gray-500 truncate">{place.address}</p>}
                </div>
              </div>
              {!connectedPlaceIds.includes(place.id) ? (
                <button onClick={() => handleAddPlace(place.id)} className="ml-2 p-1 text-blue-600 hover:bg-blue-50 rounded">
                  <Plus className="w-4 h-4" />
                </button>
              ) : (
                <Check className="ml-2 w-4 h-4 text-green-500" />
              )}
            </div>
          ))}
          {filteredPlaces.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-500">검색 결과가 없습니다</div>
          )}
        </div>
      </div>

      {/* Right Panel - Connected Places */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">
          연결된 장소 ({connectedPlaceIds.length})
        </h4>
        <div className="space-y-2">
          {connectedPlaceIds.map((placeId, index) => {
            const place = allPlaces.find(p => p.id === placeId);
            if (!place) return null;
            return (
              <div key={placeId} className="flex items-center gap-2 p-3 bg-white border border-gray-200 rounded-lg">
                <div className="flex flex-col">
                  <button onClick={() => handleMovePlaceUp(index)} disabled={index === 0} className="p-0.5 text-gray-400 hover:text-gray-600 disabled:opacity-30">
                    <ChevronUp className="w-3 h-3" />
                  </button>
                  <button onClick={() => handleMovePlaceDown(index)} disabled={index === connectedPlaceIds.length - 1} className="p-0.5 text-gray-400 hover:text-gray-600 disabled:opacity-30">
                    <ChevronDown className="w-3 h-3" />
                  </button>
                </div>
                <GripVertical className="w-4 h-4 text-gray-400" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{place.name}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 rounded">
                      {categoryLabels[place.category] || place.category}
                    </span>
                    <p className="text-xs text-gray-500">순서: {index + 1}</p>
                  </div>
                </div>
                <button onClick={() => handleRemovePlace(placeId)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                  <X className="w-4 h-4" />
                </button>
              </div>
            );
          })}
          {connectedPlaceIds.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-500">연결된 장소가 없습니다</div>
          )}
        </div>
      </div>
    </div>
  );

  // === Render: Missions Tab ===
  const renderMissionsTab = () => (
    <div className="space-y-6">
      {/* Mission Templates */}
      <div className="bg-white border border-gray-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">미션 추가하기</h4>
        <div className="grid grid-cols-3 gap-3">
          {missionTemplates.map(template => {
            const Icon = template.icon;
            return (
              <button
                key={template.type}
                onClick={() => handleAddMission(template.type)}
                className="flex flex-col items-center gap-2 p-4 border border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors"
              >
                <Icon className="w-6 h-6 text-blue-600" />
                <span className="text-sm font-medium text-gray-900">{template.name}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Connected Missions (existing) */}
      <div className="space-y-4">
        {connectedMissions.map((mission, index) => (
          <div key={mission.id} className="bg-white border border-gray-200 rounded-lg p-4">
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-sm font-medium">
                  {index + 1}
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">{mission.name}</p>
                  <p className="text-xs text-gray-500">{missionTypeLabels[mission.type] || mission.type} · 기존 미션</p>
                </div>
              </div>
              <button onClick={() => handleRemoveExistingMission(mission.id)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
            <div className="grid grid-cols-2 gap-4 text-sm">
              {mission.place && (
                <div>
                  <span className="text-xs text-gray-500">연결 장소:</span>
                  <span className="ml-1 text-gray-900">{mission.place.name}</span>
                </div>
              )}
              {mission.type === 'quiz' && mission.question && (
                <div className="col-span-2">
                  <span className="text-xs text-gray-500">질문:</span>
                  <span className="ml-1 text-gray-900">{mission.question}</span>
                </div>
              )}
              {mission.type === 'stay_time' && mission.stayMinutes && (
                <div>
                  <span className="text-xs text-gray-500">체류 시간:</span>
                  <span className="ml-1 text-gray-900">{mission.stayMinutes}분</span>
                </div>
              )}
            </div>
          </div>
        ))}

        {/* New Missions (drafts) */}
        {newMissions.map((mission, idx) => (
          <div key={mission.tempId} className="bg-white border border-blue-300 rounded-lg p-4">
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center text-green-600 text-sm font-medium">
                  새
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">{mission.name}</p>
                  <p className="text-xs text-gray-500">{missionTypeLabels[mission.type] || mission.type} · 새 미션</p>
                </div>
              </div>
              <button onClick={() => handleRemoveNewMission(mission.tempId)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <label className="block text-xs font-medium text-gray-700 mb-1">미션 이름</label>
                <input
                  type="text"
                  value={mission.name}
                  onChange={e => handleUpdateNewMission(mission.tempId, { name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">연결 장소</label>
                <select
                  value={mission.placeId || ''}
                  onChange={e => handleUpdateNewMission(mission.tempId, { placeId: e.target.value ? parseInt(e.target.value) : null })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">장소 선택</option>
                  {connectedPlaceIds.map(pid => {
                    const place = allPlaces.find(p => p.id === pid);
                    return place ? <option key={pid} value={pid}>{place.name}</option> : null;
                  })}
                </select>
              </div>

              {mission.type === 'quiz' && (
                <>
                  <div className="col-span-2">
                    <label className="block text-xs font-medium text-gray-700 mb-1">퀴즈 질문</label>
                    <input
                      type="text"
                      value={mission.question}
                      onChange={e => handleUpdateNewMission(mission.tempId, { question: e.target.value })}
                      placeholder="예: 이 건물의 이름은 무엇인가요?"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div className="col-span-2">
                    <label className="block text-xs font-medium text-gray-700 mb-1">정답</label>
                    <input
                      type="text"
                      value={mission.answer}
                      onChange={e => handleUpdateNewMission(mission.tempId, { answer: e.target.value })}
                      placeholder="예: 63빌딩"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </>
              )}

              {mission.type === 'stay_time' && (
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">체류 시간 (분)</label>
                  <input
                    type="number"
                    value={mission.stayMinutes || 5}
                    onChange={e => handleUpdateNewMission(mission.tempId, { stayMinutes: parseInt(e.target.value) || 5 })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min="1"
                  />
                </div>
              )}
            </div>
          </div>
        ))}

        {connectedMissions.length === 0 && newMissions.length === 0 && (
          <div className="text-center py-12 text-sm text-gray-500 bg-gray-50 border border-gray-200 rounded-lg">
            연결된 미션이 없습니다. 위에서 미션을 추가해주세요.
          </div>
        )}
      </div>
    </div>
  );

  // === Render: Stamps Tab ===
  const renderStampsTab = () => (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <p className="text-sm text-gray-600">이 프로그램에 사용할 스탬프를 설정하세요</p>
        <div className="relative">
          <button
            onClick={() => setShowStampDropdown(!showStampDropdown)}
            className="flex items-center gap-2 px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-4 h-4" />
            스탬프 추가
            <ChevronDown className="w-4 h-4" />
          </button>
          {showStampDropdown && (
            <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg z-10">
              <button
                onClick={() => { handleAddStamp(); setShowStampDropdown(false); }}
                className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2 border-b border-gray-100"
              >
                <Plus className="w-4 h-4 text-blue-600" />
                새 스탬프 만들기
              </button>
              <button
                onClick={() => { setShowStampLibrary(true); setShowStampDropdown(false); }}
                className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2"
              >
                <Upload className="w-4 h-4 text-blue-600" />
                기존 스탬프 가져오기
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Existing stamps */}
        {connectedStamps.map(stamp => (
          <div key={stamp.id} className="bg-white border border-gray-200 rounded-lg p-4">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                  <Stamp className="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">{stamp.name}</p>
                  <p className="text-xs text-gray-500">{conditionLabels[stamp.conditionType] || stamp.conditionType}</p>
                </div>
              </div>
              <button onClick={() => handleRemoveExistingStamp(stamp.id)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
            {stamp.conditionDetail && (
              <p className="text-xs text-gray-500 mb-2">{stamp.conditionDetail}</p>
            )}
            {stamp.place && (
              <div className="text-xs text-gray-500">연결 장소: <span className="text-gray-700">{stamp.place.name}</span></div>
            )}
          </div>
        ))}

        {/* New stamps (drafts) */}
        {newStamps.map(stamp => (
          <div key={stamp.tempId} className="bg-white border border-blue-300 rounded-lg p-4">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
                  <Stamp className="w-5 h-5 text-green-600" />
                </div>
                <p className="text-sm font-medium text-gray-900">새 스탬프</p>
              </div>
              <button onClick={() => handleRemoveNewStamp(stamp.tempId)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">스탬프 이름</label>
                <input
                  type="text"
                  value={stamp.name}
                  onChange={e => handleUpdateNewStamp(stamp.tempId, { name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">스탬프 설명</label>
                <input
                  type="text"
                  value={stamp.conditionDetail}
                  onChange={e => handleUpdateNewStamp(stamp.tempId, { conditionDetail: e.target.value })}
                  placeholder="스탬프에 대한 설명"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">발급 조건</label>
                <select
                  value={stamp.conditionType}
                  onChange={e => handleUpdateNewStamp(stamp.tempId, { conditionType: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {Object.entries(conditionLabels).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">연결 장소</label>
                <select
                  value={stamp.placeId || ''}
                  onChange={e => handleUpdateNewStamp(stamp.tempId, { placeId: e.target.value ? parseInt(e.target.value) : null })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">선택하세요</option>
                  {connectedPlaceIds.map(pid => {
                    const place = allPlaces.find(p => p.id === pid);
                    return place ? <option key={pid} value={pid}>{place.name}</option> : null;
                  })}
                </select>
              </div>
            </div>
          </div>
        ))}
      </div>

      {connectedStamps.length === 0 && newStamps.length === 0 && (
        <div className="text-center py-12 text-sm text-gray-500 bg-gray-50 border border-gray-200 rounded-lg">
          등록된 스탬프가 없습니다. 스탬프를 추가해주세요.
        </div>
      )}
    </div>
  );

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">프로그램 연결 설정</h2>
        <p className="text-sm text-gray-500 mt-1">프로그램의 장소, 미션, 스탬프를 설정하고 구성합니다</p>
      </div>

      {/* Program Selector */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">프로그램 선택</label>
          <select
            value={selectedProgramId || ''}
            onChange={e => setSelectedProgramId(e.target.value ? parseInt(e.target.value) : null)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">프로그램을 선택하세요</option>
            {programs.map(program => (
              <option key={program.id} value={program.id}>{program.name}</option>
            ))}
          </select>
        </div>

        {selectedProgram && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div>
                <p className="text-xs text-gray-500 mb-1">프로그램명</p>
                <p className="text-sm font-medium text-gray-900">{selectedProgram.name}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">운영 기간</p>
                <p className="text-sm font-medium text-gray-900">
                  {new Date(selectedProgram.startDate).toLocaleDateString()} ~ {new Date(selectedProgram.endDate).toLocaleDateString()}
                </p>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">상태</p>
                <span className={`inline-block text-xs px-2 py-1 rounded ${
                  selectedProgram.status === 'in_progress' ? 'bg-green-100 text-green-800' :
                  selectedProgram.status === 'scheduled' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {statusLabels[selectedProgram.status] || selectedProgram.status}
                </span>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">카테고리</p>
                <p className="text-sm font-medium text-gray-900">
                  {programCategoryLabels[selectedProgram.category] || selectedProgram.category}
                </p>
              </div>
            </div>
            {selectedProgram.description && (
              <div className="mt-3 pt-3 border-t border-blue-200">
                <p className="text-xs text-gray-500 mb-1">설명</p>
                <p className="text-sm text-gray-700">{selectedProgram.description}</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Tabs + Content */}
      {selectedProgramId && (
        <>
          <div className="bg-white rounded-lg border border-gray-200">
            <div className="border-b border-gray-200">
              <div className="flex overflow-x-auto">
                {tabs.map(tab => {
                  const Icon = tab.icon;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`flex items-center gap-2 px-6 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                        activeTab === tab.id
                          ? 'border-blue-600 text-blue-600'
                          : 'border-transparent text-gray-500 hover:text-gray-700'
                      }`}
                    >
                      <Icon className="w-4 h-4" />
                      {tab.label}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="p-6">
              {isLoading ? (
                <div className="flex items-center justify-center py-12">
                  <Loader2 className="w-6 h-6 text-blue-600 animate-spin" />
                  <span className="ml-2 text-sm text-gray-500">데이터를 불러오는 중...</span>
                </div>
              ) : (
                <>
                  {activeTab === 'places' && renderPlacesTab()}
                  {activeTab === 'missions' && renderMissionsTab()}
                  {activeTab === 'stamps' && renderStampsTab()}
                </>
              )}
            </div>
          </div>

          {/* Save Button */}
          <div className="fixed bottom-8 right-8">
            <button
              onClick={handleSave}
              disabled={isSaving}
              className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg shadow-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400"
            >
              {isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
              {isSaving ? '저장 중...' : '저장하기'}
            </button>
          </div>
        </>
      )}

      {/* Stamp Library Modal */}
      {showStampLibrary && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[80vh] overflow-hidden flex flex-col">
            <div className="p-6 border-b border-gray-200">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">스탬프 라이브러리</h3>
                <button onClick={() => { setShowStampLibrary(false); setSelectedLibraryStampIds([]); }} className="p-1 text-gray-400 hover:text-gray-600">
                  <X className="w-5 h-5" />
                </button>
              </div>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="스탬프 검색..."
                  value={librarySearchTerm}
                  onChange={e => setLibrarySearchTerm(e.target.value)}
                  className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
              <div className="border border-gray-200 rounded-lg overflow-hidden">
                <table className="w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-12">선택</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">스탬프명</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">발급 조건</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">연결 장소</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {libraryStamps.map(stamp => (
                      <tr
                        key={stamp.id}
                        className="hover:bg-gray-50 cursor-pointer"
                        onClick={() => {
                          if (selectedLibraryStampIds.includes(stamp.id)) {
                            setSelectedLibraryStampIds(selectedLibraryStampIds.filter(id => id !== stamp.id));
                          } else {
                            setSelectedLibraryStampIds([...selectedLibraryStampIds, stamp.id]);
                          }
                        }}
                      >
                        <td className="px-4 py-3">
                          <input type="checkbox" checked={selectedLibraryStampIds.includes(stamp.id)} onChange={() => {}} className="w-4 h-4 text-blue-600 border-gray-300 rounded" />
                        </td>
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">{stamp.name}</td>
                        <td className="px-4 py-3 text-sm text-gray-600">{conditionLabels[stamp.conditionType] || stamp.conditionType}</td>
                        <td className="px-4 py-3 text-sm text-gray-500">{stamp.place?.name || '-'}</td>
                      </tr>
                    ))}
                    {libraryStamps.length === 0 && (
                      <tr>
                        <td colSpan={4} className="px-4 py-8 text-center text-sm text-gray-500">가져올 수 있는 스탬프가 없습니다</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="p-6 border-t border-gray-200 flex items-center justify-between">
              <p className="text-sm text-gray-600">{selectedLibraryStampIds.length}개 선택됨</p>
              <div className="flex gap-2">
                <button
                  onClick={() => { setShowStampLibrary(false); setSelectedLibraryStampIds([]); }}
                  className="px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
                >
                  취소
                </button>
                <button
                  onClick={handleAddLibraryStamps}
                  disabled={selectedLibraryStampIds.length === 0}
                  className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  선택한 스탬프 추가
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
