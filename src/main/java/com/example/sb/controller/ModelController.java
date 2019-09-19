package com.example.sb.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/activiti")
@Slf4j
public class ModelController implements ModelDataJsonConstants { // act-model动作N合一
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping("/model/list") // 列出所有保存在数据库中的模板
	public Object list(int offset, int limit) {
		List<Model> list = repositoryService.createModelQuery().listPage(offset, limit);
		return list;
	}

	@RequestMapping("/model/create") // modeler.html是一个空页面，需要传入一些值填充显示
	public void createModel(HttpServletRequest request, HttpServletResponse response) {
		try {
			Model modelData = repositoryService.newModel();// 初始化一个空模型
			ObjectMapper objectMapper = new ObjectMapper();

			String modelName = "modelName";
			String modelKey = "modelKey";
			String description = "description";
			int revision = 1;

			ObjectNode modelObjectNode = objectMapper.createObjectNode();
			modelObjectNode.put(MODEL_NAME, modelName);
			modelObjectNode.put(MODEL_DESCRIPTION, description);
			modelObjectNode.put(MODEL_REVISION, revision);
			modelData.setName(modelName);
			modelData.setKey(modelKey);
			modelData.setMetaInfo(modelObjectNode.toString());
			repositoryService.saveModel(modelData); // 保存模型

			// 完善ModelEditorSource
			ObjectNode editorNode = objectMapper.createObjectNode();
			editorNode.put("id", "canvas");
			editorNode.put("resourceId", "canvas");
			ObjectNode stencilSetNode = objectMapper.createObjectNode();
			stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
			editorNode.set("stencilset", stencilSetNode);
			repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
			response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.OK)
	public void saveModel(@PathVariable String modelId, String name, String description, String json_xml,
			String svg_xml) {
		try {
			Model model = repositoryService.getModel(modelId);
			ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
			modelJson.put(MODEL_NAME, name);
			modelJson.put(MODEL_DESCRIPTION, description);
			model.setMetaInfo(modelJson.toString());
			model.setName(name);
			repositoryService.saveModel(model);
			repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));

			InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
			TranscoderInput input = new TranscoderInput(svgStream);
			PNGTranscoder transcoder = new PNGTranscoder();
			// Setup output
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			TranscoderOutput output = new TranscoderOutput(outStream);
			// Do the transformation
			transcoder.transcode(input, output);
			final byte[] result = outStream.toByteArray();
			repositoryService.addModelEditorSourceExtra(model.getId(), result);
			outStream.close();
		} catch (Exception e) {
			log.error("Error saving model", e);
			throw new ActivitiException("Error saving model", e);
		}
	}

	@GetMapping("/model/export/{id}")
	public void exportToXml(@PathVariable("id") String id, HttpServletResponse response) {
		try {
			org.activiti.engine.repository.Model modelData = repositoryService.getModel(id);
			BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
			JsonNode editorNode = new ObjectMapper()
					.readTree(repositoryService.getModelEditorSource(modelData.getId()));
			BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
			BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
			byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
			ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
			IOUtils.copy(in, response.getOutputStream());
			String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.flushBuffer();
		} catch (Exception e) {
			throw new ActivitiException("导出model的xml文件失败，模型ID=" + id, e);
		}
	}

	@GetMapping("/model/edit/{id}")
	public void edit(HttpServletResponse response, @PathVariable("id") String id) {
		try {
			response.sendRedirect("/modeler.html?modelId=" + id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 这个应该是页面里回调获取模板选项的样式返回json之类的
	@RequestMapping(value = "/editor/stencilset", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	public String getStencilset() {
		InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
		try {
			return IOUtils.toString(stencilsetStream, "utf-8");
		} catch (Exception e) {
			throw new ActivitiException("Error while loading stencil set", e);
		}
	}

	// 这个应该是页面里回调获取{modelId}模板的方法
	// @RequestMapping(value = "/{modelId}/json", method = RequestMethod.GET,
	// produces = "application/json")
	@GetMapping(value = "/model/{modelId}/json")
	public ObjectNode getEditorJson(@PathVariable String modelId) {
		ObjectNode modelNode = null;
		Model model = repositoryService.getModel(modelId);
		if (model != null) {
			try {
				if (StringUtils.isNotEmpty(model.getMetaInfo())) {
					modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
				} else {
					modelNode = objectMapper.createObjectNode();
					modelNode.put(MODEL_NAME, model.getName());
				}
				modelNode.put(MODEL_ID, model.getId());
				ObjectNode editorJsonNode = (ObjectNode) objectMapper
						.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
				modelNode.set("model", editorJsonNode);
			} catch (Exception e) {
				log.error("Error creating model JSON", e);
				throw new ActivitiException("Error creating model JSON", e);
			}
		}
		return modelNode;
	}

	@PostMapping("/model/deploy/{id}")
	public Object deploy(@PathVariable("id") String id) throws Exception {
		// 获取模型
		Model modelData = repositoryService.getModel(id);
		byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
		if (bytes == null) {
			return "模型数据为空，请先设计流程并成功保存，再进行发布。";
		}
		JsonNode modelNode = new ObjectMapper().readTree(bytes);
		BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
		if (model.getProcesses().size() == 0) {
			return "数据模型不符要求，请至少设计一条主线流程。";
		}
		byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
		// 发布流程
		String processName = modelData.getName() + ".bpmn20.xml";
		Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
				.addString(processName, new String(bpmnBytes, "UTF-8")).deploy();
		modelData.setDeploymentId(deployment.getId());
		repositoryService.saveModel(modelData);
		return "ok";
	}

	@DeleteMapping("/model/{id}")
	public Object remove(@PathVariable("id") String id) {
		repositoryService.deleteModel(id);
		return "ok";
	}

	@PostMapping("/model/batchRemove")
	public Object batchRemove(@RequestParam("ids") String[] ids) {
		for (String id : ids) {
			repositoryService.deleteModel(id);
		}
		return "ok";
	}

}