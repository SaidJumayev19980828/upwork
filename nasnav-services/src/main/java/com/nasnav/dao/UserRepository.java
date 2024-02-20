package com.nasnav.dao;

import com.nasnav.dto.UserFollow;
import com.nasnav.dto.UserListFollowProjection;
import com.nasnav.persistence.UserEntity;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findById(Long id);

	boolean existsByIdAndOrganizationId(Long id, Long orgId);

	UserEntity getByEmailAndOrganizationId(String email, Long orgId);

	boolean existsByResetPasswordToken(String resetPasswordToken);

	Optional<UserEntity> getByResetPasswordToken(String resetPasswordToken);

	boolean existsByAuthenticationToken(String authenticationToken);

	UserEntity findByResetPasswordToken(String token);

	UserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	boolean existsByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	Optional<UserEntity> findByIdAndOrganizationId(Long id, Long orgId);

	List<UserEntity> findByOrganizationId(Long orgId);

	@Query(value = "SELECT PHONE_NUMBER FROM users where id = :userId and organization_id= :organizationId"
	, nativeQuery = true)
	Optional<String> findPhoneNumberByIdAndOrganizationId(Long userId, Long organizationId);

	List<UserEntity> findByOrganizationId(Long orgId, Pageable pageable);

	List<UserEntity> findByOrganizationIdAndUserStatus(Long orgId,Integer userStatus, Pageable pageable);

	@Query("select u from UserEntity u join YeshteryUserEntity yu on u.yeshteryUserId = yu.id where u.organizationId = :orgId")
	Set<UserEntity> findAllLinkedToYeshteryUserByOrgId(Long orgId);

	@Query("select u from UserEntity u join YeshteryUserEntity yu on u.yeshteryUserId = yu.id")
	Set<UserEntity> findAllLinkedToYeshteryUser();

	@Query("select count (u.id) from UserEntity u " +
			" where u.organizationId = :orgId and u.creationTime between :minMonth and :maxMonth and u.creationTime is not null ")
	Long getNewCustomersCountPerMonth(@Param("orgId") Long orgId,
									  @Param("minMonth") LocalDateTime minMonth,
									  @Param("maxMonth") LocalDateTime maxMonth);

	List<UserEntity> findByYeshteryUserIdNotNullAndAllowReward(Boolean allowReward);

	UserEntity getByMobileAndOrganizationId(String mobile, Long orgId);

	boolean existsByMobileIgnoreCaseAndOrganizationId(String mobile, Long orgId);
	@Query("select u from UserEntity u " +
			" left join OrganizationEntity o on u.organizationId = o.id"+
			" where LOWER(u.email) = LOWER(:email) and u.organizationId = :orgId and o.yeshteryState = 1")
	UserEntity getYeshteryUserByEmail(@Param("email")String email,
									  @Param("orgId")Long orgId);

	@Modifying
	@Query("update UserEntity user set user.tier.id = :tierId where user.id = :userId")
	void updateUserTier(@Param("tierId") Long tierId, @Param("userId") Long userId);

	List<UserEntity> findByYeshteryUserId(Long yeshteryUserId);

	@Query("select user1 from UserEntity user1 join UserEntity user2 on user1.yeshteryUserId = user2.yeshteryUserId "
			+ "where user2.id = :userId and user2.yeshteryUserId is not null")
	List<UserEntity> findByYeshteryUserIdOfUserId(Long userId);

	@Query("select u from UserEntity u left join YeshteryUserEntity y on u.yeshteryUserId = CAST (y.referral as int) " +
			"where y.id = :yeshteryUserId and u.organizationId = :orgId and y.referral is not null ")
	UserEntity findByReferralUserIdAndOrganizationId(@Param("yeshteryUserId") Long yeshteryUserId,
													 @Param("orgId") Long orgId);

	void deleteByYeshteryUserId(Long yeshteryUserId);

	Optional<UserEntity> findByEmailAndOrganizationId(String email, Long orgId);

	@Query("select distinct u from UserEntity u where u.yeshteryUserId = :yeshteryUserId and u.organizationId = :orgId")
	Optional<UserEntity> findByYeshteryUserIdAndOrganizationId(@Param("yeshteryUserId") Long yeshteryUserId,
															   @Param("orgId") Long orgId);

	List<UserEntity> findByTier_Id(Long tierId);


	List<UserEntity> findAllUsersByUserStatus(Integer userStatus,PageRequest  pageRequest);

	@Transactional
	@Modifying
	@Query(value = "update users set tier_id = :tierId where organization_id = :orgId and tier_id is null", nativeQuery = true)
	void updateUsersTiers(@Param("tierId") Long tierId, @Param("orgId") Long orgId);

	@Query("SELECT u as user, " +
			"CASE WHEN f.user.id = :userId THEN true ELSE false END as isFollowing, " +
			"CASE WHEN f.follower.id = :userId THEN true ELSE false END as isFollowed " +
			"FROM UserEntity u " +
			"LEFT JOIN FollowerEntity f ON (u.id = f.user.id AND f.follower.id = :userId) OR (u.id = f.follower.id AND f.user.id = :userId)" +
			"where u.id <>	:userId" )
	PageImpl<UserListFollowProjection> findUsersWithFollowerStatus(@Param("userId") Long userId , Pageable pageable);

}
