package com.sooo.pdfmaker.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sooo.pdfmaker.domain.TagStrVO;
import com.sooo.pdfmaker.mapper.PdfMakerMapper;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@AllArgsConstructor
public class PdfMakerServiceImpl implements PdfMakerService {

	@Setter(onMethod_ = {@Autowired})
	PdfMakerMapper mapper;
	
	@Override
	public List<TagStrVO> getTagStr() {
		// TODO Auto-generated method stub
		return mapper.getTagStr();
	}

	@Override
	public List<TagStrVO> getTagList() {
		// TODO Auto-generated method stub
		return mapper.getTagList();
	}

	@Override
	public List<TagStrVO> getSettingList() {
		// TODO Auto-generated method stub
		return mapper.getSettingList();
	}

	@Override
	public List<TagStrVO> getRefSetting() {
		// TODO Auto-generated method stub
		return mapper.getRefSetting();
	}

	@Override
	public List<TagStrVO> getDefSetting() {
		// TODO Auto-generated method stub
		return mapper.getDefSetting();
	}

	@Override
	public List<TagStrVO> getDescription() {
		// TODO Auto-generated method stub
		return mapper.getDescription();
	}

}
