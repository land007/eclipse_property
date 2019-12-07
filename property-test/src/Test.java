
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReadProperties languages_zh = new ReadProperties("zh");
		ReadProperties languages_ja = new ReadProperties("ja");
		ReadProperties languages_de = new ReadProperties("de");
		
		System.out.println("最新活动");
		System.out.println("最新活动");
		System.out.println(languages_ja.get("zui4xin1huo2dong4-0"));
		System.out.println(languages_de.get("zui4xin1huo2dong4-0"));
		
		
		CmsVelocityTemplate cmsVelocityTemplate = new CmsVelocityTemplate();
		cmsVelocityTemplate.addModel("languages", languages_zh);
		cmsVelocityTemplate.parseTemplateToFile("act.vm", "act.html", false);
		
	}

}
