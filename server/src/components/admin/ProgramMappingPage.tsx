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

const categoryLabels: Record<string, string> = {
  food: '맛집',
  exhibition: '전시',
  seminar: '세미나',
  event: '이벤트',
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

const missionTypeIcons: Record<string, typeof MapPin> = {
  location_auth: MapPin,
  quiz: HelpCircle,
  stay_time: Clock,
};

const conditionLabels: Record<string, string> = {
  mission_complete: '미션 완료',
  event_participate: '이벤트 참여',
  place_visit: '장소 방문',
  quiz_correct: '퀴즈 정답',
};

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
  const [allMissions, setAllMissions] = useState<Mission[]>([]);
  const [connectedMissionIds, setConnectedMissionIds] = useState<number[]>([]);
  const [missionSearchTerm, setMissionSearchTerm] = useState('');
  const [missionTypeFilter, setMissionTypeFilter] = useState('');

  // Stamps tab
  const [allStamps, setAllStamps] = useState<StampData[]>([]);
  const [connectedStampIds, setConnectedStampIds] = useState<number[]>([]);
  const [stampSearchTerm, setStampSearchTerm] = useState('');
  const [stampConditionFilter, setStampConditionFilter] = useState('');

  // UI state
  const [isSaving, setIsSaving] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Track original connected IDs for unlink calculation
  const [originalMissionIds, setOriginalMissionIds] = useState<number[]>([]);
  const [originalStampIds, setOriginalStampIds] = useState<number[]>([]);

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
      const opts = { cache: 'no-store' as RequestCache };
      const [mappingRes, placesRes, missionsRes, stampsRes] = await Promise.all([
        fetch(`/api/v1/admin/program-mapping/${programId}`, opts).then(r => r.json()),
        fetch('/api/v1/admin/places', opts).then(r => r.json()),
        fetch('/api/v1/admin/missions', opts).then(r => r.json()),
        fetch('/api/v1/admin/stamps', opts).then(r => r.json()),
      ]);

      if (placesRes.success) setAllPlaces(placesRes.data);
      if (missionsRes.success) setAllMissions(missionsRes.data);
      if (stampsRes.success) setAllStamps(stampsRes.data);

      if (mappingRes.success) {
        const placeIds = mappingRes.data.places.map((p: Place) => p.id);
        const missionIds = mappingRes.data.missions.map((m: Mission) => m.id);
        const stampIds = mappingRes.data.stamps.map((s: StampData) => s.id);

        setConnectedPlaceIds(placeIds);
        setConnectedMissionIds(missionIds);
        setConnectedStampIds(stampIds);
        setOriginalMissionIds(missionIds);
        setOriginalStampIds(stampIds);
      }
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
  const handleAddMission = (missionId: number) => {
    if (!connectedMissionIds.includes(missionId)) {
      setConnectedMissionIds([...connectedMissionIds, missionId]);
    }
  };

  const handleRemoveMission = (missionId: number) => {
    setConnectedMissionIds(connectedMissionIds.filter(id => id !== missionId));
  };

  // Stamp handlers
  const handleAddStamp = (stampId: number) => {
    if (!connectedStampIds.includes(stampId)) {
      setConnectedStampIds([...connectedStampIds, stampId]);
    }
  };

  const handleRemoveStamp = (stampId: number) => {
    setConnectedStampIds(connectedStampIds.filter(id => id !== stampId));
  };

  // Save
  const handleSave = async () => {
    if (!selectedProgramId) return;
    setIsSaving(true);

    // Calculate unlinked missions/stamps
    const unlinkMissions = originalMissionIds.filter(id => !connectedMissionIds.includes(id));
    const unlinkStamps = originalStampIds.filter(id => !connectedStampIds.includes(id));

    try {
      const res = await fetch(`/api/v1/admin/program-mapping/${selectedProgramId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          places: {
            placeIds: connectedPlaceIds,
          },
          missions: {
            unlink: unlinkMissions,
            link: connectedMissionIds,
          },
          stamps: {
            unlink: unlinkStamps,
            link: connectedStampIds,
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

  // Filtered lists
  const filteredPlaces = allPlaces.filter(place => {
    if (placeSearchTerm && !place.name.toLowerCase().includes(placeSearchTerm.toLowerCase())) return false;
    if (placeCategoryFilter && place.category !== placeCategoryFilter) return false;
    return true;
  });

  const filteredMissions = allMissions.filter(mission => {
    if (missionSearchTerm && !mission.name.toLowerCase().includes(missionSearchTerm.toLowerCase())) return false;
    if (missionTypeFilter && mission.type !== missionTypeFilter) return false;
    return true;
  });

  const filteredStamps = allStamps.filter(stamp => {
    if (stampSearchTerm && !stamp.name.toLowerCase().includes(stampSearchTerm.toLowerCase())) return false;
    if (stampConditionFilter && stamp.conditionType !== stampConditionFilter) return false;
    return true;
  });

  const tabs = [
    { id: 'places' as TabType, label: '장소 연결', icon: MapPin, count: connectedPlaceIds.length },
    { id: 'missions' as TabType, label: '미션 연결', icon: Target, count: connectedMissionIds.length },
    { id: 'stamps' as TabType, label: '스탬프 설정', icon: Stamp, count: connectedStampIds.length },
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
    <div className="grid grid-cols-2 gap-6">
      {/* Left Panel - Available Missions */}
      <div className="bg-white border border-gray-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">등록된 미션</h4>
        <div className="relative mb-3">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="미션 검색..."
            value={missionSearchTerm}
            onChange={e => setMissionSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <select
          value={missionTypeFilter}
          onChange={e => setMissionTypeFilter(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
        >
          <option value="">전체 유형</option>
          {Object.entries(missionTypeLabels).map(([key, label]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>

        <div className="space-y-2 max-h-96 overflow-y-auto">
          {filteredMissions.map(mission => {
            const Icon = missionTypeIcons[mission.type] || Target;
            return (
              <div key={mission.id} className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                    <Icon className="w-4 h-4 text-blue-600" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{mission.name}</p>
                    <div className="flex items-center gap-2 mt-1">
                      <span className="text-xs px-2 py-0.5 bg-purple-100 text-purple-800 rounded">
                        {missionTypeLabels[mission.type] || mission.type}
                      </span>
                      {mission.place && (
                        <span className="text-xs text-gray-500 truncate">{mission.place.name}</span>
                      )}
                    </div>
                  </div>
                </div>
                {!connectedMissionIds.includes(mission.id) ? (
                  <button onClick={() => handleAddMission(mission.id)} className="ml-2 p-1 text-blue-600 hover:bg-blue-50 rounded">
                    <Plus className="w-4 h-4" />
                  </button>
                ) : (
                  <Check className="ml-2 w-4 h-4 text-green-500" />
                )}
              </div>
            );
          })}
          {filteredMissions.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-500">검색 결과가 없습니다</div>
          )}
        </div>
      </div>

      {/* Right Panel - Connected Missions */}
      <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">
          연결된 미션 ({connectedMissionIds.length})
        </h4>
        <div className="space-y-2">
          {connectedMissionIds.map((missionId, index) => {
            const mission = allMissions.find(m => m.id === missionId);
            if (!mission) return null;
            const Icon = missionTypeIcons[mission.type] || Target;
            return (
              <div key={missionId} className="flex items-center gap-3 p-3 bg-white border border-gray-200 rounded-lg">
                <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center text-purple-600 text-sm font-medium flex-shrink-0">
                  {index + 1}
                </div>
                <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                  <Icon className="w-4 h-4 text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{mission.name}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-xs px-2 py-0.5 bg-purple-100 text-purple-800 rounded">
                      {missionTypeLabels[mission.type] || mission.type}
                    </span>
                    {mission.place && (
                      <span className="text-xs text-gray-500">{mission.place.name}</span>
                    )}
                    {mission.type === 'stay_time' && mission.stayMinutes && (
                      <span className="text-xs text-gray-500">{mission.stayMinutes}분</span>
                    )}
                  </div>
                </div>
                <button onClick={() => handleRemoveMission(missionId)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                  <X className="w-4 h-4" />
                </button>
              </div>
            );
          })}
          {connectedMissionIds.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-500">연결된 미션이 없습니다</div>
          )}
        </div>
      </div>
    </div>
  );

  // === Render: Stamps Tab ===
  const renderStampsTab = () => (
    <div className="grid grid-cols-2 gap-6">
      {/* Left Panel - Available Stamps */}
      <div className="bg-white border border-gray-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">등록된 스탬프</h4>
        <div className="relative mb-3">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="스탬프 검색..."
            value={stampSearchTerm}
            onChange={e => setStampSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <select
          value={stampConditionFilter}
          onChange={e => setStampConditionFilter(e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
        >
          <option value="">전체 조건</option>
          {Object.entries(conditionLabels).map(([key, label]) => (
            <option key={key} value={key}>{label}</option>
          ))}
        </select>

        <div className="space-y-2 max-h-96 overflow-y-auto">
          {filteredStamps.map(stamp => (
            <div key={stamp.id} className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50">
              <div className="flex items-center gap-3 flex-1 min-w-0">
                <div className="w-8 h-8 rounded-lg bg-amber-50 flex items-center justify-center flex-shrink-0">
                  <Stamp className="w-4 h-4 text-amber-600" />
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{stamp.name}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-800 rounded">
                      {conditionLabels[stamp.conditionType] || stamp.conditionType}
                    </span>
                    {stamp.place && (
                      <span className="text-xs text-gray-500 truncate">{stamp.place.name}</span>
                    )}
                  </div>
                </div>
              </div>
              {!connectedStampIds.includes(stamp.id) ? (
                <button onClick={() => handleAddStamp(stamp.id)} className="ml-2 p-1 text-blue-600 hover:bg-blue-50 rounded">
                  <Plus className="w-4 h-4" />
                </button>
              ) : (
                <Check className="ml-2 w-4 h-4 text-green-500" />
              )}
            </div>
          ))}
          {filteredStamps.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-500">검색 결과가 없습니다</div>
          )}
        </div>
      </div>

      {/* Right Panel - Connected Stamps */}
      <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-3">
          연결된 스탬프 ({connectedStampIds.length})
        </h4>
        <div className="space-y-2">
          {connectedStampIds.map((stampId) => {
            const stamp = allStamps.find(s => s.id === stampId);
            if (!stamp) return null;
            return (
              <div key={stampId} className="flex items-center gap-3 p-3 bg-white border border-gray-200 rounded-lg">
                <div className="w-8 h-8 rounded-lg bg-amber-50 flex items-center justify-center flex-shrink-0">
                  <Stamp className="w-4 h-4 text-amber-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{stamp.name}</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-800 rounded">
                      {conditionLabels[stamp.conditionType] || stamp.conditionType}
                    </span>
                    {stamp.place && (
                      <span className="text-xs text-gray-500">{stamp.place.name}</span>
                    )}
                    {stamp.conditionDetail && (
                      <span className="text-xs text-gray-500 truncate">{stamp.conditionDetail}</span>
                    )}
                  </div>
                </div>
                <button onClick={() => handleRemoveStamp(stampId)} className="p-1 text-red-600 hover:bg-red-50 rounded">
                  <X className="w-4 h-4" />
                </button>
              </div>
            );
          })}
          {connectedStampIds.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-500">연결된 스탬프가 없습니다</div>
          )}
        </div>
      </div>
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
                      {tab.count > 0 && (
                        <span className="ml-1 px-1.5 py-0.5 text-xs bg-blue-100 text-blue-700 rounded-full">
                          {tab.count}
                        </span>
                      )}
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
    </div>
  );
}
