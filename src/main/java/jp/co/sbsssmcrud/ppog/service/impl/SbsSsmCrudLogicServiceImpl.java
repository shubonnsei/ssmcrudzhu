package jp.co.sbsssmcrud.ppog.service.impl;

import java.util.List;

import org.postgresql.util.PSQLException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import jp.co.sbsssmcrud.ppog.dto.CityDto;
import jp.co.sbsssmcrud.ppog.entity.City;
import jp.co.sbsssmcrud.ppog.mapper.CityMapper;
import jp.co.sbsssmcrud.ppog.mapper.CountryMapper;
import jp.co.sbsssmcrud.ppog.mapper.LanguageMapper;
import jp.co.sbsssmcrud.ppog.service.SbsSsmCrudLogicService;
import jp.co.sbsssmcrud.ppog.utils.Messages;
import jp.co.sbsssmcrud.ppog.utils.Pagination;
import jp.co.sbsssmcrud.ppog.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 中央処理サービス実装クラス
 *
 * @author shubonnsei
 * @since 1.00
 */
@Service
@Transactional(rollbackFor = PSQLException.class)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SbsSsmCrudLogicServiceImpl implements SbsSsmCrudLogicService {

	/**
	 * ページングナビゲーションのページ数
	 */
	private static final Integer NAVIGATION_PAGES = 5;

	/**
	 * ページサイズ
	 */
	private static final Integer PAGE_SIZE = 8;

	/**
	 * デフォルトソート値
	 */
	private static final Integer SORT_NUMBER = 100;

	/**
	 * 都市マッパー
	 */
	private final CityMapper cityMapper;

	/**
	 * 国家マッパー
	 */
	private final CountryMapper countryMapper;

	/**
	 * 言語マッパー
	 */
	private final LanguageMapper languageMapper;

	@Override
	public CityDto getCityInfoById(final Integer id) {
		final City city = this.cityMapper.selectById(id);
		final String languageByCountryCode = this.languageMapper
				.getOfficialLanguageByCountryCode(city.getCountryCode());
		return new CityDto(city.getId(), city.getName(), city.getCountry().getContinent(), city.getCountry().getName(),
				city.getDistrict(), city.getPopulation(), languageByCountryCode);
	}

	@Override
	public Pagination<CityDto> getPageInfo(final Integer pageNum, final String keyword) {
		int sort = SORT_NUMBER;
		final int offset = PAGE_SIZE * (pageNum - 1);
		// キーワードの属性を判断する；
		if (StringUtils.isNotEmpty(keyword)) {
			final String hankakuKeyword = StringUtils.toHankaku(keyword);
			if (hankakuKeyword.startsWith("min(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				// 人口数量昇順で最初の15個都市の情報を吹き出します；
				final List<CityDto> minimumRanks = this.cityMapper.findMinimumRanks(sort).stream().map(item -> {
					final String language = this.languageMapper.getOfficialLanguageByCountryCode(item.getCountryCode());
					return new CityDto(item.getId(), item.getName(), item.getCountry().getContinent(),
							item.getCountry().getName(), item.getDistrict(), item.getPopulation(), language);
				}).toList();
				if (offset + PAGE_SIZE >= sort) {
					return Pagination.of(minimumRanks.subList(offset, sort), minimumRanks.size(), pageNum, PAGE_SIZE,
							NAVIGATION_PAGES);
				}
				return Pagination.of(minimumRanks.subList(offset, offset + PAGE_SIZE), minimumRanks.size(), pageNum,
						PAGE_SIZE, NAVIGATION_PAGES);
			}
			if (hankakuKeyword.startsWith("max(pop)")) {
				final int indexOf = hankakuKeyword.indexOf(")");
				final String keisan = hankakuKeyword.substring(indexOf + 1);
				if (StringUtils.isNotEmpty(keisan)) {
					sort = Integer.parseInt(keisan);
				}
				// 人口数量降順で最初の15個都市の情報を吹き出します；
				final List<CityDto> maximumRanks = this.cityMapper.findMaximumRanks(sort).stream().map(item -> {
					final String language = this.languageMapper.getOfficialLanguageByCountryCode(item.getCountryCode());
					return new CityDto(item.getId(), item.getName(), item.getCountry().getContinent(),
							item.getCountry().getName(), item.getDistrict(), item.getPopulation(), language);
				}).toList();
				if (offset + PAGE_SIZE >= sort) {
					return Pagination.of(maximumRanks.subList(offset, sort), maximumRanks.size(), pageNum, PAGE_SIZE,
							NAVIGATION_PAGES);
				}
				return Pagination.of(maximumRanks.subList(offset, offset + PAGE_SIZE), maximumRanks.size(), pageNum,
						PAGE_SIZE, NAVIGATION_PAGES);
			}
			// ページング検索；
			final String nationCode = this.countryMapper.findNationCode(hankakuKeyword);
			if (StringUtils.isNotEmpty(nationCode)) {
				final Integer cityInfosByNationCnt = this.cityMapper.countCityInfosByNation(nationCode);
				if (cityInfosByNationCnt == 0) {
					return Pagination.of(Lists.newArrayList(), 0, pageNum);
				}
				final List<CityDto> cityInfosByNation = this.cityMapper
						.getCityInfosByNation(nationCode, offset, PAGE_SIZE).stream().map(item -> {
							final String language = this.languageMapper
									.getOfficialLanguageByCountryCode(item.getCountryCode());
							return new CityDto(item.getId(), item.getName(), item.getCountry().getContinent(),
									item.getCountry().getName(), item.getDistrict(), item.getPopulation(), language);
						}).toList();
				return Pagination.of(cityInfosByNation, cityInfosByNationCnt, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
			}
			final Integer cityInfosByNameCnt = this.cityMapper.countCityInfosByName(hankakuKeyword);
			if (cityInfosByNameCnt == 0) {
				return Pagination.of(Lists.newArrayList(), 0, pageNum);
			}
			final List<CityDto> cityInfosByName = this.cityMapper.getCityInfosByName(hankakuKeyword, offset, PAGE_SIZE)
					.stream().map(item -> {
						final String language = this.languageMapper
								.getOfficialLanguageByCountryCode(item.getCountryCode());
						return new CityDto(item.getId(), item.getName(), item.getCountry().getContinent(),
								item.getCountry().getName(), item.getDistrict(), item.getPopulation(), language);
					}).toList();
			return Pagination.of(cityInfosByName, cityInfosByNameCnt, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
		}
		final Integer cityInfosCnt = this.cityMapper.countCityInfos();
		if (cityInfosCnt == 0) {
			return Pagination.of(Lists.newArrayList(), 0, pageNum);
		}
		// ページング検索；
		final List<CityDto> cityInfos = this.cityMapper.getCityInfos(offset, PAGE_SIZE).stream().map(item -> {
			final String language = this.languageMapper.getOfficialLanguageByCountryCode(item.getCountryCode());
			return new CityDto(item.getId(), item.getName(), item.getCountry().getContinent(),
					item.getCountry().getName(), item.getDistrict(), item.getPopulation(), language);
		}).toList();
		return Pagination.of(cityInfos, cityInfosCnt, pageNum, PAGE_SIZE, NAVIGATION_PAGES);
	}

	@Override
	public List<String> getListOfNationsById(final Integer id) {
		final List<String> list = Lists.newArrayList();
		final City city = this.cityMapper.selectById(id);
		final String nation = city.getCountry().getName();
		list.add(nation);
		final List<String> nations = this.countryMapper.findNationsByCnt(city.getCountry().getContinent()).stream()
				.filter(item -> StringUtils.isNotEqual(item, nation)).toList();
		list.addAll(nations);
		return list;
	}

	@Override
	public void save(final CityDto cityDto) {
		final City city = new City();
		BeanUtils.copyProperties(cityDto, city, "continent", "nation", "language");
		final Integer saiban = this.cityMapper.saiban();
		final String countryCode = this.countryMapper.findNationCode(cityDto.nation());
		city.setId(saiban);
		city.setCountryCode(countryCode);
		city.setDeleteFlg(Messages.MSG007);
		this.cityMapper.saveById(city);
	}

	@Override
	public void update(final CityDto cityDto) {
		final City city = new City();
		BeanUtils.copyProperties(cityDto, city, "continent", "nation", "language");
		final String countryCode = this.countryMapper.findNationCode(cityDto.nation());
		city.setCountryCode(countryCode);
		this.cityMapper.updateById(city);
	}

	@Override
	public void removeById(final Integer id) {
		this.cityMapper.removeById(id);
	}

	@Override
	public List<String> findAllContinents() {
		return this.countryMapper.findAllContinents();
	}

	@Override
	public List<String> findNationsByCnt(final String continentVal) {
		final String hankaku = StringUtils.toHankaku(continentVal);
		return this.countryMapper.findNationsByCnt(hankaku);
	}

	@Override
	public String findLanguageByCty(final String nationVal) {
		final String nationCode = this.countryMapper.findNationCode(StringUtils.toHankaku(nationVal));
		return this.languageMapper.getOfficialLanguageByCountryCode(nationCode);
	}

	@Override
	public Integer checkDuplicate(final String cityName) {
		return this.cityMapper.checkDuplicatedName(cityName);
	}
}
