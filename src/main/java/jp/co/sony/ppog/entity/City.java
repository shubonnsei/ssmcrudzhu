package jp.co.sony.ppog.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * 都市テーブルのエンティティ
 *
 * @author shubonnsei
 * @since 1.00
 */
@Data
public final class City implements Serializable {

	/**
	 * This field corresponds to the database column ID
	 */
	private Integer id;

	/**
	 * This field corresponds to the database column NAME
	 */
	private String name;

	/**
	 * This field corresponds to the database column COUNTRY_CODE
	 */
	private String countryCode;

	/**
	 * This field corresponds to the database column DISTRICT
	 */
	private String district;

	/**
	 * This field corresponds to the database column POPULATION
	 */
	private Integer population;

	/**
	 * This field corresponds to the database column DELETE_FLG
	 */
	private String deleteFlg;
}