package org.eclipse.xtext.xdoc.tests;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderFactory;
import org.eclipse.emf.mwe.core.resources.ResourceLoaderImpl;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xpand2.output.Output;
import org.eclipse.xpand2.output.OutputImpl;
import org.eclipse.xtend.expression.Variable;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.eclipse.xtext.junit.AbstractXtextTests;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xdoc.XdocStandaloneSetup;
import org.eclipse.xtext.xdoc.generator.Outlets;
import org.eclipse.xtext.xdoc.xdoc.AbstractSection;
import org.eclipse.xtext.xdoc.xdoc.Chapter;
import org.eclipse.xtext.xdoc.xdoc.Document;
import org.eclipse.xtext.xdoc.xdoc.TextOrMarkup;
import org.eclipse.xtext.xdoc.xdoc.TextPart;
import org.eclipse.xtext.xdoc.xdoc.XdocFactory;
import org.eclipse.xtext.xdoc.xdoc.XdocFile;

public abstract class AbstractXdocGeneratorTest extends AbstractXtextTests {

	protected static final String RESULT_DIR = "test-gen/";
	public static String EXPECTATION_DIR = Outlets.WEB_SITE_PATH_NAME + "/";
	public static String SRC_DIR = "testfiles/";
	protected ParserTest pTest;
	private XpandExecutionContextImpl xpandCtx;

	public AbstractXdocGeneratorTest() {
		super();
		File f = new File(RESULT_DIR);
		if(f.exists()) {
			deleteRecursive(f.listFiles());
		} else {
			f.mkdir();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		with(new XdocStandaloneSetup());
		getInjector().injectMembers(this);
		this.pTest = new ParserTest();
		this.pTest.setUp();

		Output output = new OutputImpl();
		Outlet outlet = new Outlet(RESULT_DIR);
		output.addOutlet(outlet);

		ResourceLoaderFactory.setCurrentThreadResourceLoader(new ResourceLoaderImpl(getClass().getClassLoader()));
		xpandCtx = new XpandExecutionContextImpl(output, null);
		Map<String, Variable> variables = xpandCtx.getGlobalVariables();
		Variable srcDir = new Variable("srcDir", SRC_DIR);
		variables.put("srcDir", srcDir);
		Variable dir = new Variable("dir", RESULT_DIR);
		variables.put("dir", dir);
		xpandCtx.registerMetaModel(new JavaBeansMetaModel());
		ResourceLoaderFactory.setCurrentThreadResourceLoader(null);
	}

	abstract protected void generate(EObject eObject);

	protected void validate(String expected, String result) throws Exception {
		FileChannel expF = new FileInputStream(expected).getChannel();
		FileChannel resultF = new FileInputStream(result).getChannel();
		ByteBuffer bExp = ByteBuffer.allocateDirect((int) expF.size());
		ByteBuffer bResult = ByteBuffer.allocateDirect((int) resultF.size());
		assertEquals(bExp.capacity(), bResult.capacity());
		expF.read(bExp);
		bExp.rewind();
		resultF.read(bResult);
		bResult.rewind();
		if(bExp.compareTo(bResult) != 0){
			for (int i = 0; bExp.hasRemaining() || bResult.hasRemaining(); i++) {
				char a = (char) bExp.get();
				char b = (char) bResult.get();
				if (a != b) {
					fail("Expected " + a +" but was " + b+ " at position " + bExp.position());
				}
			}
		}
	}

	protected XpandExecutionContext getXpandCtx() {
		return xpandCtx;
	}

	private void deleteRecursive(File... files) {
		for (File file : files) {
			if(file.isDirectory()) {
				deleteRecursive(file.listFiles());
				file.delete();
			}
			file.delete();
		}
	}


	protected Document initDocFromFile(String string, String filename) throws Exception {
		XdocFile file = pTest.getDocFromFile(SRC_DIR + filename);
		AbstractSection mainSection = file.getMainSection();
		if(mainSection instanceof Document) {
			return (Document) mainSection;
		} else if(mainSection instanceof Chapter) {
			Document doc = initDoc(string);
			doc.getChapters().add((Chapter) mainSection);
			return doc;
		}
		return null;
	}

	protected Document initDoc(String name) {
		Document result = XdocFactory.eINSTANCE.createDocument();
		TextOrMarkup tomTitle = XdocFactory.eINSTANCE.createTextOrMarkup();
		TextPart title = XdocFactory.eINSTANCE.createTextPart();
		title.setText(name);
		tomTitle.getContents().add(title);
		result.setTitle(tomTitle);
		return result;
	}

	protected Document createDocumentFrom(String mainDocument, String... docs) {
		XtextResourceSet set = get(XtextResourceSet.class);
		Resource ret = set.getResource(URI.createURI(SRC_DIR + mainDocument), true);
		for(String doc: docs) {
			set.getResource(URI.createURI(SRC_DIR + doc), true);
		}
		return (Document) ((XdocFile) getModel((XtextResource)ret)).getMainSection();
	}

	protected Chapter createChapterFrom(String file) {
		XtextResourceSet set = get(XtextResourceSet.class);
		Resource ret = set.getResource(URI.createURI(SRC_DIR + file), true);
		return (Chapter) ((XdocFile) getModel((XtextResource)ret)).getMainSection();
	}

	public abstract void testGenCodeWithLanguage() throws Exception;

	public abstract void testGenCode() throws Exception;

	public abstract void testARef() throws Exception;

	public abstract void testCodeRef() throws Exception;

	public abstract void testComment() throws Exception;

	public abstract void testImg() throws Exception;

	public abstract void testLink() throws Exception;

	public abstract void testRefText() throws Exception;

	public abstract void testNestedList() throws Exception;

	public abstract void testSimpleRef() throws Exception;

	public abstract void testEscape() throws Exception;

	public abstract void testTable() throws Exception;

	public abstract void testTwoChapters() throws Exception;

	public abstract void testFullHirarchy() throws Exception;
}