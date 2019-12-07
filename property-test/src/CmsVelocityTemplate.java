import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class CmsVelocityTemplate {
	
	private VelocityContext cmsVelocityContext;
	private static String realTemplateDir = "/Volumes/jiayq/Documents/runtime-Eclipse应用程序/test/";
	private static String velocityLogFile = "/Volumes/jiayq/Documents/runtime-Eclipse应用程序/test/velocity.log";
	
	public CmsVelocityTemplate(){
		this.cmsVelocityContext = new VelocityContext();
		Properties prop = new Properties();
		
		prop.put(Velocity.RUNTIME_LOG , velocityLogFile);
		/*配置模版目录*/

		Velocity.clearProperty(Velocity.FILE_RESOURCE_LOADER_PATH);//初始化
		prop.put(Velocity.FILE_RESOURCE_LOADER_PATH , realTemplateDir);
		prop.put(Velocity.INPUT_ENCODING , "UTF-8");//输入编码    
		prop.put(Velocity.OUTPUT_ENCODING , "UTF-8");//输出编码
		
		try {
			Velocity.init(prop);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmsVelocityTemplate cmsVelocityTemplate = new CmsVelocityTemplate();
		ReadProperties languages_ja = new ReadProperties("ja");
		cmsVelocityTemplate.addModel("language", languages_ja);
		cmsVelocityTemplate.parseTemplateToFile("act.vm", "act.html", false);
	}
	
	public void addModel(String name,Object value){
		cmsVelocityContext.put(name, value);
	}
	
	/**
	 * 方法描述:parseTemplateToFile 
	 * 创建日期：2011-3-10
	 * 最后修改日期:2011-3-10
	 * @param templateFilePath
	 * @param targetFile
	 * @param clearContext 是否清理现有的velocityContext中的数据
	 * @return
	 * @version
	 */
	public boolean parseTemplateToFile(String templateFilePath , String targetFile,boolean clearContext){
		Template templateObj;
		boolean success = false;
		
		try {
			/**get template file*/
			templateObj = Velocity.getTemplate(templateFilePath,"UTF-8");
			StringWriter sw =new StringWriter();

			/** parse data with templateFile **/
			templateObj.merge(cmsVelocityContext, sw);
			
			/** 将内容写入到制定的文件中(覆盖式写入)*/
//			FileUtils.writeToFile(targetFile, sw.toString());
			
			org.apache.commons.io.FileUtils.write(new File(targetFile), sw.toString(),"UTF-8");
			success=true;
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			
			if(clearContext){
				/**清除现有的velocityContext里面的数据*/
				for(Object key : cmsVelocityContext.getKeys()){
					cmsVelocityContext.remove(key);
				}
			}
			
		}

		
		return success;
	}

}
