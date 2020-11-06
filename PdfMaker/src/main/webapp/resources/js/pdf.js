var defSizeList = {
	maxWidth : 595.27563,
	maxHeight : 841.8898,
	fontNormal : 1.1791992,
	fontBold : 1.2016602
};
var elementList = [];
var seqList = [];
var pdfScale = 0.7;

var mainTag;
var subTag;
var tagStrList;

var refSetting;
var defSetting;
var description;

$(document).ready(function() {
	init();
});

function init() {

	$.ajax({
		url : "./getTagStr.do",
		type : "GET",
		dataType : "json",
//		async : false,
		contentType : "application/text; charset=UTF-8",
		success : function(result) {

			mainTag = filterJson(result.tag, "flag", "PDF_BODY");
			subTag = filterJson(result.tag, "flag", "CSS");
			
			tagStrList = result.tagStr;
	
			refSetting = result.refSetting;
			defSetting = result.defSetting;
			description = result.description;
			
			addBtn();
			setBtnActivation();
		},
		error : function(request, status, error) {
			console.log(error);
		}
	});
	
	$(".div-add-element").css("width", defSizeList.maxWidth * pdfScale);
	$("#box_page").css("width", defSizeList.maxWidth * pdfScale);
	$("#box_page").css("height", defSizeList.maxHeight * pdfScale);
}

/**
 * @returns
 */
function addBtn() {

	var boxAddBtn = $("#box_addbtn");
	var cssModal = $("#css_modal");
	var keyList = [ "tagCode", "tagName" ];
	
	// PDF 요소 추가
	for ( var i in mainTag) {
		boxAddBtn.append(applyToFormat("TAG_BTN_01", makeValueList(mainTag[i],
				keyList)));
	}

	// 모달에 SUB TAG 추가
	for ( var i in subTag) {
		cssModal.append(applyToFormat("TAG_BTN_02", makeValueList(
				subTag[i], keyList)));
	}
}

/**
 * @param code
 * @returns
 */
function clickAddElement(code) {
	
	var tempTag = filterJson(mainTag, "tagCode", code)[0];
	
	var tagCode = tempTag.strCode;
	var treeCode = tempTag.treeCode;
	var cssCode = tempTag.cssCode;
	
	var id, pageSeq, tagName;
	var parentObj, treeObj;
	var tagValue, element;

	// 요소추가 행, 열
	if (code.indexOf("tr") != -1 || code.indexOf("td") != -1) {

		var row, col;
		var trTag = filterJson(mainTag, "tagCode", "tr")[0];
		var tdTag = filterJson(mainTag, "tagCode", "td")[0];

		parentObj = $(".sel-table");
		id = parentObj.attr("id") + "_";
		if (code.indexOf("tr") != -1) {
			treeObj = $("#" + parentObj.attr("id") + "_div");
			row = elementList[parentObj.attr("id")].row + 1;
			col = elementList[parentObj.attr("id")].col;

			tagName = tempTag.tagName;
			elementList[parentObj.attr("id")].row = row;

		} else if (code.indexOf("td") != -1) {
			row = elementList[parentObj.attr("id")].row;
			col = elementList[parentObj.attr("id")].col + 1;
			tagName = tempTag.tagName;
			elementList[parentObj.attr("id")].col = col;
		}

		for (var r = 0; r < row; r++) {
			
			var rowId = id + r;
			
			tagName = trTag.tagName + r;
			if ($("#" + rowId).length == 0) {
				tagValue = {
					"id" : rowId,
					"tagName" : tagName
				};
				element = {
					"cssCode" : "tr",
					"tagCode" : "tr",
					"text" : tagName,
					"row" : r
				};
				addElement("tr", tagValue, element, parentObj, treeObj);
			}
			var sCol = parentObj.children().eq(r).children().length;
			for (var c = sCol; c < col; c++) {
				var pObj = parentObj.children().eq(r);
				var tObj = $("#" + pObj.attr("id") + "_div");
				tagName = tdTag.tagName + r + "_" + c;
				tagValue = {
					"id" : rowId + "_" + c,
					"tagName" : tagName
				};
				element = {
					"cssCode" : "SET_TD_01",
					"tagCode" : "td",
					"text" : tagName,
					"row" : r,
					"col" : c
				};
				addElement("td", tagValue, element, pObj, tObj);
			}
		}

	} else {
		if (code.indexOf("page") != -1) {
			$(".div-sel-page").toggleClass("div-sel-page"); // 현재 표시된 페이지 숨김처리
			parentObj = $("#box_page");
			treeObj = $("#box_tree");
			pageSeq = parentObj.children().length;
			tagName = tempTag.tagName + pageSeq;
			id = code + "_" + pageSeq;
			tagValue = {
				"id" : id,
				"tagName" : tagName
			};
			element = {
				"cssCode" : cssCode,
				"text" : tagName,
				"tagCode" : code,
				"seqNo" : pageSeq
			};

			if ($("#btn_download").attr("disabled") == "disabled") {
				$("#btn_download").attr("disabled", false);
				$("#btn_download").removeClass("btn-add-disable");
				$("#btn_download").addClass("btn-add-element");
			}
		} else {
			parentObj = $(".div-sel-page");
			treeObj = $("#" + parentObj.attr("id") + "_div");
			var listId = parentObj.attr("id");
			
			if (elementList[listId][code] == null) {
				elementList[listId][code] = [];
			}
			
			var eleSeqNo = elementList[listId][code].length;
			pageSeq = elementList[listId].seqNo;
			tagName = tempTag.tagName + (eleSeqNo + 1);
			id = code + "_" + pageSeq + "_" + eleSeqNo;
			elementList[listId][code][eleSeqNo] = "tree_" + id;
			tagValue = {
				"id" : id,
				"tagName" : tagName
			};
			element = {
				"cssCode" : cssCode,
				"text" : tagName,
				"tagCode" : code
			};
		}

		addElement(code, tagValue, element, parentObj, treeObj);
		// 테이블 row, col 추가
		if (code.toLowerCase().indexOf("table") != -1) {
			elementList[id].row = 0;
			elementList[id].col = 0;

			clickAddElement("tr");
			clickAddElement("tr");
			clickAddElement("td");
			clickAddElement("td");
		}

		clickElement(id);
		setBtnActivation();
	}
}

/**
 * @param code
 * @param tagValue
 * @param element
 * @param pObj
 * @param tObj
 * @returns
 */
function addElement(code, tagValue, element, pObj, tObj) {

	var tempTag = filterJson(mainTag, "tagCode", code)[0];
	var tagCode = tempTag.strCode;
	var treeCode = tempTag.treeCode;
	var cssCode = tempTag.cssCode;

	var tempSet = filterJson(defSetting, "tagCode", tempTag.tagCode);

	elementList[tagValue.id] = element;

	for ( var i in tempTag) {
		var codeToLower = code.toLowerCase();
		var parentToLower = tempTag.parentCode.toLowerCase();
		if (parentToLower.indexOf(codeToLower) != -1) {
			elementList[tagValue.id][i] = [];
		}
	}

	pObj.append(applyToFormat(tagCode, tagValue)); // 새 페이지 추가
	tObj.append(applyToFormat(treeCode, tagValue)); // 트리추가	

	if (tempTag.parentCode != "ROOT"
			&& $("#" + pObj.attr("id") + "_div").attr("class").indexOf(
					"div-tree-hide") != -1) {
		toggleFold(pObj.attr("id") + "_div"); // 트리 상태변경
	}

	setStyle(tagValue.id, new Array(), tempSet);

	$(".sel-" + code).removeClass("sel-" + code);
	$("#" + tagValue.id).addClass("sel-" + code);
}

/**
 * @param id
 * @param cssValue
 * @param defValue
 * @returns
 */
function setStyle(id, editValue, defValue) {

	for ( var i in defValue) {
		var tempValue = defValue[i];
		var property = tempValue.property;
		
		if (tempValue.value != null) {
			
			var value = editValue[i];
				value = value == null ? tempValue.value : value;
				value = parseValue(value);

			if (value == null || value.length == 0) {
				continue;
			}
			$("#" + id).css(property, value);
		}
	}
}

/**
 * @param id
 * @returns
 */
function toggleFold(id) {
	var obj = $("#" + id);
	obj.toggleClass("div-tree-hide");
	obj.toggleClass("div-tree");

	var src = $("#" + id + "_img").attr("src");
	src = src.indexOf("plus") != -1 ? src.replace("plus", "sub") : src.replace(
			"sub", "plus");
	$("#" + id + "_img").attr("src", src);
}

/**
 * @param key
 * @param value
 * @returns
 */
function parseValue(value) {
	
	var keyValue = value.split(":");
	var refSet = filterJson(refSetting, "refCode", keyValue[0])[0];
	var suffix = refSet.suffix == null ? "" : refSet.suffix;
	
	var val = keyValue[1].split(",");
	for (var i = 0; i < val.length; i++) {
		if (refSet.refCode == "px") {
			val[i] = val[i].replace("px", "") * pdfScale;
		}
		val[i] = val[i] + suffix;
	}
	return val;
}

/**
 * @param id
 * @returns
 */
function clickElement(id) {
	
	if (id.indexOf("page") != -1) {
		$(".div-sel-page").removeClass("div-sel-page");
		$(".sel-table").removeClass("sel-table");
		$("#" + id).addClass("div-sel-page");
	}

	$("#inneredit_tree").children().remove();

	if (elementList[id] != null) {
	
		var code = elementList[id].tagCode;
		$(".sel-" + code).removeClass("sel-" + code);
		$("#" + id).addClass("sel-" + code);
		
		var tempSet = filterJson(defSetting, "tagCode", elementList[id].tagCode);
		var cssCode = elementList[id].cssCode;
		var title = elementList[id].text;
	
		var valueList = {"id" : id, "title" : elementList[id].text};
		
		$("#inneredit_tree").append(applyToFormat("TAG_EDIT_TITLE", valueList)); // 타이틀 텍스트

//		var brIdx = title.indexOf("\n");
//		if (brIdx != -1) {
//			$("#now_" + id).text(title.substring(0, brIdx));
//		}
		
		var tempDef = filterJson(defSetting, "tagCode", code); // 변경 가능한 속성 및 기본값
		for (var i in tempDef) {

			var tempDescription = filterJson(description, "desCode", tempDef[i].desCode)[0];
			var refList = tempDef[i].refCode.split(";");
			
			var keyVal = nvl(elementList[id][tempDef.property], tempDef[i].value);
			var key, val;
			if (keyVal != null) {
				keyVal = keyVal.split(":");
				key = keyVal[0];
				val = keyVal[1];
			}
			
//			<div id='tree_#id#'><button style="cursor:pointer; background:none; border:0;" onclick="toggleFold('#id#_div')"><img id='#id#_div_img' src='/resources/img/tree-icon-plus.png' width='8px'/></button><button id=tree_#id#_btn' class='btn-tree text-sm' onclick="clickElement('#id#')">#tagName#</button></div><div id='#id#_div' class='div-tree-hide'></div>
			
			var subValueList = {"id" : tempDef[i].property, "hanName" : tempDescription.hanName};
			$("#inneredit_tree").append(applyToFormat("SET_AREA", subValueList)); // 속성 영역별
			
			for (var j in refList) {
				var tempRef = filterJson(refSetting, "refCode", refList[j])[0];
				var tempDesc = filterJson(description, "desCode", tempRef.desCode)[0];
				
				$("#" + tempDef[i].property + "_button").append("<button style='display:block'>" + tempDesc.hanName + "</button>");
				if (tempRef.type == "COMBO") {
					var inputStr = "<select>";
					var valArr = tempRef.value.split(";");
					for (var k in valArr) {
						inputStr += "<option value='" + valArr[k] + "'>" + valArr[k] + "</option>";
					}
					inputStr += "</select>";
					
					$("#inneredit_tree").append(inputStr);
				}
				else {
					var inputCnt = 1;
					var subVal = "";
					if (tempRef.type.indexOf("_") != -1) {
						inputCnt = tempRef.type.substring(tempRef.type.indexOf("_") + 1);
						subVal = val.split(",");
					}

					for (var k = 0; k < inputCnt; k++) {
	 					$("#inneredit_tree").append("<input>");
					}
				}
			}
		}
//		for ( var i in defStyleList[cssCode]) {
//	
//			if (i == "setCode") {
//				continue;
//			}
//			var value = elementList[id][i];
//			value = value == null ? defStyleList[cssCode][i] : value;
//			
//			valueList.cssName = i;
//			valueList.value = value;
//			$("#inneredit_tree").append(applyToFormat("TAG_EDIT_CSS", valueList));
//		}
//		setStyle("now_" + id, elementList[id], defStyleList[cssCode]);
//		setStyle("now_" + id, elementList[id], defStyleList["EDIT_DEFALUT"]);

		$("#inneredit_tree").append(applyToFormat("TAG_EDIT_TEXTAREA", valueList));
		$("#inneredit_tree").append(applyToFormat("TAG_EDIT_DELBTN", valueList));
	}
	setBtnActivation();
}

function nvl(obj1, obj2) {
	return obj1 != null ? obj1 : obj2;
}

/**
 * @param id
 * @returns
 */
function delElement(id) {
	if (elementList[id].tagCode == "page") {
		$("#" + id).remove();
		$("#box_" + id).remove();	
		var tempIdx = $("#box_page").children().length == 1 ? 0 : 1;
		clickElement($("#box_page").children().eq(tempIdx).attr("id"));
		
		if (tempIdx == 0) {
			$("#btn_download").attr("disabled", true);
			$("#btn_download").addClass("btn-add-disable");
			$("#btn_download").removeClass("btn-add-element");
		}
	}
	else {
		$("#" + id).remove();
		$("#box_" + id).remove();	
		clickElement($(".sel-page").attr("id"));
	}
	setBtnActivation();
}

/**
 * @param event
 * @param id
 * @returns
 */
function changeStyle(event, id) {

	var cssCode = elementList[id].cssCode;
	var targetId = event.target.id.replace("edit_", "");

	if (targetId == "colspan" || targetId == "rowspan") {

		var indList = id.replaceAll("table_", "").split("_");
		var row = parseInt(indList[2]);
		var col = parseInt(indList[3]);
		
		var colspan = parseInt($("#" + id).attr("colspan"));
		var rowspan = parseInt($("#" + id).attr("rowspan"));
		colspan = isNaN(colspan) ? 1 : colspan;
		rowspan = isNaN(rowspan) ? 1 : rowspan;

		var preValue = parseInt(event.target.defaultValue);
		var newValue = parseInt(event.target.value);

		preValue = isNaN(preValue) ? 1 : preValue;
		newValue = isNaN(newValue) ? 1 : newValue;
		
		if (!isNaN(newValue)) {
			var sRow, eRow, sCol, eCol;
			var displayVal = "none";

			if (targetId == "colspan") {
				if (preValue > newValue) {
					displayVal = "";
					sCol = col + newValue;
					eCol = col + preValue;
				} else {
					sCol = col + preValue;
					eCol = col + newValue;
				}

				sRow = row;
				eRow = sRow + rowspan;
				$("#" + id).attr("colspan", newValue);
			} else if (targetId == "rowspan") {

				if (preValue > newValue) { // 병합 해제
					displayVal = "";
					sRow = row + newValue;
					eRow = row + preValue;
				} else {
					sRow = row + preValue;
					eRow = row + newValue;
				}

				sCol = col;
				eCol = sCol + colspan;
				$("#" + id).attr("rowspan", newValue);
			}

			for (var i = sRow; i < eRow; i++) {
				for (var j = sCol; j < eCol; j++) {
					$("#table_" + indList[0] + "_" + indList[1] + "_" + i+ "_" + j).css("display", displayVal);
					$("#box_table_" + indList[0] + "_" + indList[1] + "_" + i + "_" + j).css("display", displayVal);
				}
			}
			elementList[id][targetId] = newValue;
			event.target.defaultValue = event.target.value;
		}

	} else {
		var value = elementList[id][targetId];
		value = value == null ? defStyleList[cssCode][targetId] : value;
		var newValue = $("#edit_" + targetId).val();
		if (value != newValue) {
			elementList[id][targetId] = newValue;

			newValue = parseValue(newValue);
			console.log(newValue);
			if (targetId == "font_size") {
				newValue *= pdfScale;
				if (defStyleList[cssCode]["font-weight"] == "bold") {
					newValue *= defSizeList.fontBold;
				} else {
					newValue *= defSizeList.fontNormal;
				}
			}
			else if (typeof(newValue) == "number") {
				newValue *= pdfScale;
			}

			$("#" + id).css(targetId.replace("_", "-"), newValue);
			if ("padding,font-size,align".indexOf(targetId) == -1) {
				$("#edit_title").css(targetId, newValue);
			}
		}
	}
}

/**
 * @param type
 * @returns
 */
function addSubTag(type) {
	var tagStr = tagStrList[subTagList[type].strCode].tagStr;

	var textArea = document.getElementById('edit_textarea');
	var startCur = textArea.selectionStart;
	var endCur = textArea.selectionEnd;

	var text = textArea.value;

	var forward = text.substring(0, startCur);
	var backward = text.substring(endCur);
	var replace = tagStr.replaceAll("#text#", text.substring(startCur, endCur));
	textArea.value = forward + replace + backward;

	changeText($(".modi_now").attr("id").replace("now_", ""));
}

/**
 * @param id
 * @returns
 */
function changeText(id) {
	var text = $("#edit_textarea").val();
	var innerTextTag = tagStrList["TAG_INNER_TEXT_01"].tagStr;

	elementList[id].text = text;

	// 개행 있으면 edit title에 첫 줄만 보이도록 > 개행없이 아주 긴 경우 처리 필요
	var brIdx = text.indexOf("\n");
	if (brIdx == -1) {
		$("#edit_title").text(text);
	}
	else {
		$("#edit_title").text(text.substring(0, brIdx));
	}
	
	$("#tree_" + id + "_btn").text(text);

	$("#" + id).children().remove();
	$("#" + id).append(
			innerTextTag.replace("#tagName#", text.replaceAll("\n", "<br>")));
}

/**
 * @param event
 * @returns
 */
function dragEndTextArea(event) {

	var textArea = document.getElementById('edit_textarea');
	var startCur = textArea.selectionStart;
	var endCur = textArea.selectionEnd;

	if (event.type == "mouseup" && startCur != endCur) {
		$("#css_modal").css("display", "grid");
		$("#css_modal").css("top", event.pageY);
		$("#css_modal").css("left", event.pageX);
	} else {
		$("#css_modal").css("display", "none");
	}
}

/**
 * @param id
 * @returns
 */
function dragStart(id) {
	$("#" + id).addClass("drag");
	$("#del_guide").addClass("div-del-guide-act1");
}

/**
 * @param event
 * @param id
 * @returns
 */
function dragEnd(event, id) {

	var dragId = $(".drag").attr("id");
	var docRect = document.getElementById($(".div-sel-page").attr("id"))
			.getBoundingClientRect();
	
	if (docRect.bottom + docRect.top < event.screenY) { // 삭제 이벤트시
		delElement(dragId);
	}
	else { // 순서 변경
		var selId = $(".div-sel-page").attr("id");
		var selPage = document.getElementById($(".div-sel-page").attr("id"));
		var dragElement = document.getElementById($(".drag").attr("id"));

		var selPage2 = document.getElementById(selId + "_div");
		var dragElement2 = document.getElementById("box_" + dragId);

		var underDragId = $(".border-underdrag").attr("id");
		var overDragId = $(".border-overdrag").attr("id");

		if (underDragId != null) {
			var underDrag = document.getElementById(underDragId);
			var underDrag2 = document.getElementById("box_" + underDragId);

			if (underDrag.nextSibling == null) {
				selPage.append(dragElement);
				selPage2.append(dragElement2);
			} else {
				selPage.insertBefore(dragElement, underDrag.nextSibling);
				selPage2.insertBefore(dragElement2, underDrag2.nextSibling);
			}
		} else if (overDragId != null) {
			var overDrag = document.getElementById(overDragId);
			var overDrag2 = document.getElementById("box_" + overDragId);
			selPage.insertBefore(dragElement, overDrag);
			selPage2.insertBefore(dragElement2, overDrag2);
		}
	}
	
	$("#del_guide").removeClass("div-del-guide-act2");
	$("#del_guide").removeClass("div-del-guide-act1");
	$(".drag").toggleClass("drag", "");
	$("#" + underDragId).removeClass("border-underdrag");
	$("#" + overDragId).removeClass("border-overdrag");
}

/**
 * @param id
 * @returns
 */
function dragEnter(id) {
	$(".border-underdrag").removeClass("border-underdrag");
	$(".border-overdrag").removeClass("border-overdrag");
	$("#" + id).addClass("border-underdrag");
	
	$("#del_guide").addClass("div-del-guide-act1");
	$("#del_guide").removeClass("div-del-guide-act2");
}

/**
 * @param event
 * @param id
 * @returns
 */
function dragLeave(event, id) {
	
	if (event.layerY > document.getElementById($(".div-sel-page").attr("id"))
	.getBoundingClientRect().bottom) {
		$("#del_guide").addClass("div-del-guide-act2");
		$("#del_guide").removeClass("div-del-guide-act1");
		$(".border-underdrag").removeClass("border-underdrag");
	}
	else {
		var topY = document.getElementById($(".div-sel-page").attr("id")).firstChild
		.getBoundingClientRect().top;
		
		if (elementList[id].tagCode != "page" && event.layerY > 0 && topY > event.layerY) {
			$(".border-underdrag").removeClass("border-underdrag");
			$(".border-overdrag").removeClass("border-overdrag");
			$("#" + id).addClass("border-overdrag");
		}
	}
	
}

/**
 * @param tagInfo
 * @param keyList
 * @returns
 */
function makeValueList(tagInfo, keyList) {
	var valueList = new Array();
	for ( var i in keyList) {
		valueList[keyList[i]] = tagInfo[keyList[i]];
	}
	return valueList;
}

/**
 * @param tagId
 * @param tagValue
 * @returns
 */
function applyToFormat(tagId, tagValue) {
	
	var strObj = filterJson(tagStrList, "strCode", tagId)[0];
	var innerObj = filterJson(tagStrList, "strCode", "TAG_INNER_TEXT_01")[0];
	var DragObj = filterJson(tagStrList, "strCode", "CSS_DRAG_01")[0];
	
	var tempStr = strObj.tagStr;
	
	tempStr = tempStr.replaceAll("#innerText#", innerObj.tagStr);
	tempStr = tempStr.replaceAll("#dragStr#", DragObj.tagStr);

	for ( var i in tagValue) {
		tempStr = tempStr.replaceAll("#" + i + "#", tagValue[i]);
	}
	return tempStr;
}

//다운로드
/**
 * @param id
 * @returns
 */
function download(id) {
	var pdfBody = makeTxt('box_page');

	console.log(pdfBody);
//	$("#downloadParam").attr("value", pdfBody);
//	$("#downloadSubmit").submit();
}

function filterJson(json, key, value) {
	return json.filter(function(object) {
		return object[key] === value;
	});
}

/**
 * @param id
 * @returns
 */
function makeTxt(id) {

	var obj = document.getElementById(id).children;
	var str = "";
	
	for (var i = 0; i < obj.length; i++) {
		var cId = obj.item(i).id;
		if (cId == "page_empty") {
			continue;
		}
		else if (cId.length == 0) {
			var rtnText = obj.item(i).innerText.replaceAll("\n", "<br>");
			str += (rtnText == null ? "" : rtnText);
		}
		else {
			var cType = elementList[cId].tagCode;
			if (cType != "page" && $("#" + cId).css("display") == "none") {
				continue;
			}
			str += "\n";
			str += makeTagStr(cId, "sTag");
			
			if (obj.item(i).children.length > 0) {
				str += makeTxt(cId);
			}
			str += makeTagStr(cId, "eTag");

		}
	}
	return str;
}

/**
 * @param id
 * @param flag
 * @returns
 */
function makeTagStr(id, flag) {
	var element = elementList[id];
	var tempTag = filterJson(mainTag, "tagCode", elementList[id].tagCode)[0];
	var tempSet = filterJson(defSetting, "tagCode", elementList[id].tagCode);

	var str = "";
	if (element != null) {
		if (flag == "sTag") {
			str = tempTag.tagType;
//			for (var name in defStyleList[element.cssCode]) {
			for (var i in tempSet) {
				var name = tempSet[i].property;
//				if (name == "setCode") {
//					continue;
//				}
				
				var value = elementList[id][name];
				value = value == null || value.length == 0 ? tempSet[name] : value;
				if (value != null && value.length != 0){
					str += " ";
					str += name + "=\"";
					str += value + "\"";
				}
			}
			console.log(str);
		}
		else if ("eTag") {
			str = "/"
				+ tempTag.tagType;
		}
	}
	str = "<" + str + ">";
	return str;
}

/**
 * @returns
 */
function setBtnActivation() {
//	var mainTag = filterJson(tagList, "flag", "PDF_BODY");

	for (var i in mainTag) {
		var selId = ".sel-" + mainTag[i].parentCode.toLowerCase();
		var btnId = "#btn_add_" + mainTag[i].tagCode;
		
		if (mainTag[i].parentCode == "ROOT" || $(selId).length != 0) {
			$(btnId).attr("disabled", false);
			$(btnId).removeClass("btn-add-disable");
			$(btnId).addClass("btn-add-element");
		}
		else {
			$(btnId).attr("disabled", true);
			$(btnId).addClass("btn-add-disable");
			$(btnId).removeClass("btn-add-element");
		}
	}
}

function trim(str) {
	return str.replace(/^\s+|\s+$/g, "");
}