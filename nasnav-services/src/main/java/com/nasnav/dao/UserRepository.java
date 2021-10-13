package com.nasnav.dao;

import com.nasnav.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findById(Long id);

	/**
	 * Get UserEntity by passed email and orgId.
	 *
	 * @param email email
	 * @param orgId orgId
	 * @return UserEntity
	 */
	UserEntity getByEmailAndOrganizationId(String email, Long orgId);

	@Query("select u.organizationId from UserEntity u where u.id = :userId")
	Long findUserOrganizationId(@Param("userId") Long userId);

	/**
	 * Check if the passed resetPasswordToken already exist before or not.
	 *
	 * @param resetPasswordToken to be checked
	 * @return true if esetPasswordToken already exists
	 */
	boolean existsByResetPasswordToken(String resetPasswordToken);

	/**
	 * Get userEntity by resetPasswordToken.
	 *
	 * @param resetPasswordToken
	 * @return UserEntity
	 */
	Optional<UserEntity> getByResetPasswordToken(String resetPasswordToken);

	/**
	 * Check if the passed authenticationToken already exist before or not.
	 *
	 * @param authenticationToken to be checked
	 * @return true if authenticationToken already exists
	 */
	boolean existsByAuthenticationToken(String authenticationToken);
	
	Optional<UserEntity> findByAuthenticationToken(String authToken);

	UserEntity findByResetPasswordToken(String token);

	UserEntity getByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	boolean existsByEmailIgnoreCaseAndOrganizationId(String email, Long orgId);

	Optional<UserEntity> findByIdAndOrganizationId(Long id, Long orgId);

	List<UserEntity> findByOrganizationId(Long orgId);

	@Query("select count (u.id) from UserEntity u " +
			" where u.organizationId = :orgId and u.creationTime between :minMonth and :maxMonth and u.creationTime is not null ")
	Long getNewCustomersCountPerMonth(@Param("orgId") Long orgId,
									  @Param("minMonth") LocalDateTime minMonth,
									  @Param("maxMonth") LocalDateTime maxMonth);

	List<UserEntity> getByOrganizationIdAndAllowReward(Long orgId, Boolean allowReward);

	/**
	 * Get UserEntity by passed mobile and orgId.
	 *
	 * @param mobile mobile
	 * @param orgId orgId
	 * @return UserEntity
	 */
	UserEntity getByMobileAndOrganizationId(String mobile, Long orgId);

	boolean existsByMobileIgnoreCaseAndOrganizationId(String mobile, Long orgId);
	@Query("select u from UserEntity u " +
			" left join OrganizationEntity o on u.organizationId = o.id"+
			" where LOWER(u.email) = LOWER(:email) and u.organizationId = :orgId and o.yeshteryState = 1")
	UserEntity getYeshteryUserByEmail(@Param("email")String email,
									  @Param("orgId")Long orgId);

	List<UserEntity> getByFamily_IdAndOrganizationId(Long familyId, Long orgId);

	@Query("update UserEntity user set user.family.id = :familyId where user.id = :userId")
	void updateUserWithFamilyId(@Param("familyId") Long familyId, @Param("userId") Long userId);

	@Query("update UserEntity user set user.tier.id = :tierId where user.id = :userId")
	void updateUserWithTierId(@Param("tierId") Long tierId, @Param("userId") Long userId);

	List<UserEntity> findByYeshteryUserId(Long yeshteryUserId);

    void deleteByYeshteryUserId(Long yeshteryUserId);

    List<UserEntity> findByFamily_Id(Long familyId);

    Optional<UserEntity> findByEmailAndOrganizationId(String email, Long orgId);
}
