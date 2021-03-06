package com.clinomics.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.clinomics.entity.lims.Bundle;
import com.clinomics.entity.lims.Member;
import com.clinomics.entity.lims.Sample;
import com.clinomics.entity.lims.SampleHistory;
import com.clinomics.enums.ResultCode;
import com.clinomics.enums.StatusCode;
import com.clinomics.repository.lims.BundleRepository;
import com.clinomics.repository.lims.MemberRepository;
import com.clinomics.repository.lims.ProductRepository;
import com.clinomics.repository.lims.SampleHistoryRepository;
import com.clinomics.repository.lims.SampleRepository;
import com.clinomics.specification.lims.SampleSpecification;
import com.clinomics.util.CustomIndexPublisher;
import com.google.common.collect.Maps;

@Service
public class SampleService {

	@Autowired
	SampleRepository sampleRepository;

	@Autowired
	SampleHistoryRepository sampleHistoryRepository;

	@Autowired
	BundleRepository bundleRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	DataTableService dataTableService;
	
	@Autowired
	SampleItemService sampleItemService;

	@Autowired
	CustomIndexPublisher customIndexPublisher;
	
	public Map<String, Object> findAll() {
		int draw = 1;
		long total = sampleRepository.count();
		List<Sample> list = sampleRepository.findAll();
		
		return dataTableService.getDataTableMap(draw, draw, total, total, list);
	}
	
	public Map<String, Object> findSampleByBundleAndDateResultNullStatusFail(Map<String, String> params) {
		int draw = 1;
		// #. paging param
		int pageNumber = NumberUtils.toInt(params.get("pgNmb"), 1);
		int pageRowCount = NumberUtils.toInt(params.get("pgrwc"), 10);
		
		List<Order> orders = Arrays.asList(new Order[] { Order.desc("createdDate"), Order.asc("id") });
		// #. paging 관련 객체
		Pageable pageable = Pageable.unpaged();
		if (pageRowCount > 1) {
			pageable = PageRequest.of(pageNumber, pageRowCount, Sort.by(orders));
		}
		long total;
		
		Specification<Sample> where = Specification
					.where(SampleSpecification.betweenDate(params))
					.and(SampleSpecification.bundleId(params))
					.and(SampleSpecification.keywordLike(params))
					.and(
						SampleSpecification.notExistsResult()
//						.or(SampleSpecification.existsResultStatusIn(Arrays.asList(StatusCode.FAIL))
						.or(SampleSpecification.notExistsResultNotEqualStatus(StatusCode.ANLS_FAIL))
					);
					
		
		total = sampleRepository.count(where);
		Page<Sample> page = sampleRepository.findAll(where, pageable);
		
		List<Sample> list = page.getContent();
		List<Map<String, Object>> header = sampleItemService.filterItemsAndOrdering(list, BooleanUtils.toBoolean(params.getOrDefault("all", "false")));
		long filtered = total;
		
		return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list, header);
	}
	
	public Map<String, Object> findSampleByBundleAndDate(Map<String, String> params) {
		int draw = 1;
		// #. paging param
		int pageNumber = NumberUtils.toInt(params.get("pgNmb"), 1);
		int pageRowCount = NumberUtils.toInt(params.get("pgrwc"), 10);
		
		List<Order> orders = Arrays.asList(new Order[] { Order.desc("createdDate"), Order.asc("id") });
		// #. paging 관련 객체
		Pageable pageable = Pageable.unpaged();
		if (pageRowCount > 1) {
			pageable = PageRequest.of(pageNumber, pageRowCount, Sort.by(orders));
		}
		long total;
		
		Specification<Sample> where = Specification
					.where(SampleSpecification.betweenDate(params))
					.and(SampleSpecification.bundleId(params))
					.and(SampleSpecification.keywordLike(params));
					
		total = sampleRepository.count(where);
		Page<Sample> page = sampleRepository.findAll(where, pageable);
		
		List<Sample> list = page.getContent();
		sampleItemService.filterItemsAndOrdering(list, BooleanUtils.toBoolean(params.getOrDefault("all", "false")));
		long filtered = total;
		
		return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list);
	}
	
	@Transactional
	public Map<String, String> save(Map<String, String> datas) {
		Map<String, String> rtn = Maps.newHashMap();
		
		String id = datas.getOrDefault("id", "");
		Optional<Sample> oSample = sampleRepository.findById(id);
		
		Sample news = new Sample();
		LocalDateTime nnow = LocalDateTime.now();
		news.setCreatedDate(nnow);
		news.setModifiedDate(nnow);
		Sample sample = oSample.orElse(news);
		boolean existsSample = Optional.ofNullable(sample.getId()).isPresent();
		
		Bundle bundle;
		if (existsSample) {
			if (!sampleHistoryRepository.existsBySample_Id(id)) {
				saveSampleHistory(sample);
			}
			bundle = sample.getBundle();
			sample.setModifiedDate(nnow);
		} else {
			if (!datas.containsKey("bundleId")) {
				rtn.put("result", ResultCode.FAIL_NOT_EXISTS.get());
				return rtn;
			}
			int bundleId = NumberUtils.toInt((String)datas.get("bundleId"));
			Optional<Bundle> oBundle = bundleRepository.findById(bundleId);
			bundle = oBundle.orElseThrow(NullPointerException::new);
			
			Optional<Member> oMember = memberRepository.findById(datas.getOrDefault("memberId", ""));
			Member member = oMember.orElseThrow(NullPointerException::new);
			sample.setCreatedMember(member);
		}
		
		datas.remove("memberId");
		datas.remove("bundleId");
		
		if (!existsSample) {
			if (bundle.isAutoBarcode()) {
				String bar = customIndexPublisher.getNextBarcodeByBundle(bundle);
				if (!bar.isEmpty()) sample.setBarcode(bar);
			} else {
				sample.setBarcode(datas.get("barcode"));
			}
			
			// #. TODO
			// if (bundle.isAutoSequence()) {
			// 	String seq = customIndexPublisher.getNextSequenceByBundle(bundle);
			// 	if (!seq.isEmpty()) sample.setId(seq);
			// } else {
			// 	sample.setId(datas.get("laboratory"));
			// }
		}
		datas.remove("laboratory");
		datas.remove("barcode");
		datas.remove("id");
		
		Map<String, Object> newItems = Maps.newHashMap();
		newItems.putAll(datas);
		sample.setBundle(bundle);
		sample.setItems(newItems);
		
		sampleRepository.save(sample);
		if (existsSample) {
			saveSampleHistory(sample);
			System.out.println(">>");
		}

		rtn.put("result", ResultCode.SUCCESS.get());
		return rtn;
	}
	
	private void saveSampleHistory(Sample smpl) {
		SampleHistory sh = new SampleHistory();
		sh.setSample(smpl);
		sh.setBarcode(smpl.getBarcode());
		sh.setItems(smpl.getItems());
		sh.setMember(smpl.getCreatedMember());
		
		sampleHistoryRepository.save(sh);
	}
}
