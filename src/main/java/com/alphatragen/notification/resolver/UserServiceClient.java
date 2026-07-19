package com.alphatragen.notification.resolver;

import java.util.List;

public interface UserServiceClient {
    /**
     * 특정 아파트 내 특정 사용자를 조회하여 사용자 ID 목록을 반환한다.
     */
    List<Long> findUsersByIndividual(Long apartmentId, Long userId);

    /**
     * 특정 아파트 내 특정 세대(동/호)의 사용자 ID 목록을 반환한다.
     */
    List<Long> findUsersByHousehold(Long apartmentId, String building, String unit);

    /**
     * 특정 아파트 내 특정 동의 사용자 ID 목록을 반환한다.
     */
    List<Long> findUsersByBuilding(Long apartmentId, String building);

    /**
     * 특정 아파트 내 특정 역할을 가진 사용자 ID 목록을 반환한다.
     */
    List<Long> findUsersByRole(Long apartmentId, String role);

    /**
     * 특정 아파트 전체 사용자 ID 목록을 반환한다.
     */
    List<Long> findUsersByApartment(Long apartmentId);
}
