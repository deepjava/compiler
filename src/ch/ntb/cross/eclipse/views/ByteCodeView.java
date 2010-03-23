package ch.ntb.cross.eclipse.views;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IExceptionTableEntry;
import org.eclipse.jdt.core.util.IFieldInfo;
import org.eclipse.jdt.core.util.ILocalVariableTableEntry;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.core.util.OpcodeStringValues;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.util.CodeAttribute;
import org.eclipse.jdt.internal.core.util.LineNumberAttribute;
import org.eclipse.jdt.internal.core.util.LocalVariableAttribute;
import org.eclipse.jdt.internal.core.util.SourceFileAttribute;
import org.eclipse.jdt.internal.core.util.StackMapTableAttribute;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.cross.eclipse.builder.CompiledClass;
import ch.ntb.cross.eclipse.exceptions.NotImplementedException;
import ch.ntb.cross.eclipse.reader.ClassReader;

public class ByteCodeView extends ViewPart implements ISelectionListener {
	public static final String ID = "ch.ntb.cross.eclipse.views.ByteCodeView";

	private Table hexTable;

	private HashMap<String, CompiledClass> cc;
	private IClassFileReader reader;

	private TreeItem magic, minor_version, major_version, constant_pool, access_flags, this_class, super_class, interfaces, fields, methods,
	attributes;
	private Tree tree;

	private static final int CONSTANT_CLASS = 7;
	private static final int CONSTANT_Fieldref = 9;
	private static final int CONSTANT_Methodref = 10;
	private static final int CONSTANT_InterfaceMethodref = 11;
	private static final int CONSTANT_String = 8;
	private static final int CONSTANT_Integer = 3;
	private static final int CONSTANT_Float = 4;
	private static final int CONSTANT_Long = 5;
	private static final int CONSTANT_Double = 6;
	private static final int CONSTANT_NameAndType = 12;
	private static final int CONSTANT_Utf8 = 1;

	private static final HashMap<Integer, String> CONSTANT_POOL_TAGS = new HashMap<Integer, String>();

	static {
		CONSTANT_POOL_TAGS.put(CONSTANT_CLASS, "CONSTANT_CLASS");
		CONSTANT_POOL_TAGS.put(CONSTANT_Fieldref, "CONSTANT_Fieldref");
		CONSTANT_POOL_TAGS.put(CONSTANT_Methodref, "CONSTANT_Methodref");
		CONSTANT_POOL_TAGS.put(CONSTANT_InterfaceMethodref, "CONSTANT_InterfaceMethodref");
		CONSTANT_POOL_TAGS.put(CONSTANT_String, "CONSTANT_String");
		CONSTANT_POOL_TAGS.put(CONSTANT_Integer, "CONSTANT_Integer");
		CONSTANT_POOL_TAGS.put(CONSTANT_Float, "CONSTANT_Float");
		CONSTANT_POOL_TAGS.put(CONSTANT_Long, "CONSTANT_Long");
		CONSTANT_POOL_TAGS.put(CONSTANT_Double, "CONSTANT_Double");
		CONSTANT_POOL_TAGS.put(CONSTANT_NameAndType, "CONSTANT_NameAndType");
		CONSTANT_POOL_TAGS.put(CONSTANT_Utf8, "CONSTANT_Utf8");
	}

	@Override
	public void createPartControl(Composite parent) {

		// Register Selection Service
		ISelectionService s = getSite().getWorkbenchWindow().getSelectionService();
		s.addSelectionListener(this);
		getViewSite().getPage().addSelectionListener(this);

		SashForm sashForm = new SashForm(parent, SWT.NONE);

		Composite left = new Composite(sashForm, SWT.NONE);
		left.setLayout(new FillLayout(SWT.HORIZONTAL));

		tree = new Tree(left, SWT.BORDER);

		magic = new TreeItem(tree, SWT.NONE);
		magic.setText("Magic");

		minor_version = new TreeItem(tree, SWT.NONE);
		minor_version.setText("Minor Version");

		major_version = new TreeItem(tree, SWT.NONE);
		major_version.setText("Minor Version");

		constant_pool = new TreeItem(tree, SWT.NONE);
		constant_pool.setText("LoadConstant Pool");

		access_flags = new TreeItem(tree, 0);
		access_flags.setText("Access Flags");

		this_class = new TreeItem(tree, 0);
		this_class.setText("This Class");

		super_class = new TreeItem(tree, 0);
		super_class.setText("Super Class");

		interfaces = new TreeItem(tree, 0);
		interfaces.setText("Interfaces");

		fields = new TreeItem(tree, 0);
		fields.setText("Fields");

		methods = new TreeItem(tree, 0);
		methods.setText("Methods");

		attributes = new TreeItem(tree, 0);
		attributes.setText("Attributes");

		Composite right = new Composite(sashForm, SWT.NONE);
		right.setLayout(new FillLayout(SWT.HORIZONTAL));

		hexTable = new Table(right, SWT.BORDER | SWT.FULL_SELECTION);
		hexTable.setHeaderVisible(true);
		hexTable.setLinesVisible(true);

		TableColumn tblclmnAddress = new TableColumn(hexTable, SWT.NONE);
		tblclmnAddress.setWidth(100);
		tblclmnAddress.setText("Address");
		sashForm.setWeights(new int[] { 1, 1 });

		for (int i = 0; i < 16; i++) {
			TableColumn tableColumn_1 = new TableColumn(hexTable, SWT.NONE);
			tableColumn_1.setWidth(20);
			tableColumn_1.setText(Integer.toHexString(i));
		}

	}

	@Override
	public void setFocus() {

	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof ICompilationUnit) {
				CompilationUnit cu = (CompilationUnit) first;
				try {
					IResource res = cu.getUnderlyingResource();
					String s = res.getFullPath().toString();

					s = s.replace("/src/", "/bin/");
					s = s.replace(".java", ".class");

					IPath path2 = new Path(s);
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path2);
					reader = ClassReader.parseClassFile(file);
					updateView();

				} catch (JavaModelException e) {
				}
			}
		}
	}

	private void updateView() {
		if (reader != null) {
			if (!magic.isDisposed()) {
				magic.setText("Magic : 0x" + Integer.toHexString(reader.getMagic()).toUpperCase());
			}

			if (!minor_version.isDisposed()) {
				minor_version.setText("Minor Version : " + reader.getMinorVersion());
			}

			if (!major_version.isDisposed()) {
				major_version.setText("Major Version : " + reader.getMajorVersion());
			}

			if (!constant_pool.isDisposed()) {
				constant_pool.removeAll();
				IConstantPool constantPool = reader.getConstantPool();

				for (int entry = 1; entry < constantPool.getConstantPoolCount(); entry++) {
					TreeItem item = new TreeItem(constant_pool, SWT.NONE);
					int e = constantPool.getEntryKind(entry);
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(entry);
					TreeItem tag = new TreeItem(item, SWT.NONE);
					tag.setText("u1 tag : " + e);

					switch (e) {
						case CONSTANT_CLASS: // CONSTANT_CLASS
							TreeItem classItem = new TreeItem(item, SWT.NONE);
							classItem.setText("name_index : " + constantPoolEntry.getClassInfoNameIndex() + " /* "
									+ new String(constantPoolEntry.getClassInfoName()) + " */");
							break;
						case CONSTANT_String:
							TreeItem string_index = new TreeItem(item, SWT.NONE);
							string_index.setText("u2 string_index : " + constantPoolEntry.getStringIndex() + " /* "
									+ constantPoolEntry.getStringValue() + " */");
							break;
						case CONSTANT_Integer:
							TreeItem integerBytes = new TreeItem(item, SWT.NONE);
							integerBytes.setText("u4 bytes : " + constantPoolEntry.getFloatValue());
							break;
						case CONSTANT_Float:
							TreeItem floatBytes = new TreeItem(item, SWT.NONE);
							floatBytes.setText("u4 bytes : " + constantPoolEntry.getFloatValue());
							break;
						case CONSTANT_Long:
							TreeItem longBytes = new TreeItem(item, SWT.NONE);
							longBytes.setText("u4 bytes (high & low) : " + constantPoolEntry.getLongValue());
							break;

						case CONSTANT_Double:
							TreeItem doubleBytes = new TreeItem(item, SWT.NONE);
							doubleBytes.setText("u4 bytes (high & low) : " + constantPoolEntry.getDoubleValue());
							break;
						case CONSTANT_NameAndType:
							TreeItem name_index = new TreeItem(item, SWT.NONE);
							name_index.setText("u2 name_index : " + constantPoolEntry.getNameAndTypeIndex());

							TreeItem descriptor_index = new TreeItem(item, SWT.NONE);
							descriptor_index.setText("u2 descriptor_index : " + constantPoolEntry.getNameAndTypeInfoDescriptorIndex());
							break;
						case CONSTANT_Fieldref:
						case CONSTANT_Methodref:
						case CONSTANT_InterfaceMethodref:
							TreeItem class_index = new TreeItem(item, SWT.NONE);
							class_index.setText("u2 class_index : " + constantPoolEntry.getClassIndex() + " /* "
									+ new String(constantPoolEntry.getClassName()) + " */");

							TreeItem name_and_type_index = new TreeItem(item, SWT.NONE);
							name_and_type_index.setText("u2 name_and_type_index : " + constantPoolEntry.getNameAndTypeIndex());
							break;
						case CONSTANT_Utf8:
							TreeItem length = new TreeItem(item, SWT.NONE);
							length.setText("u2 length : " + constantPoolEntry.getUtf8Length());

							TreeItem bytes = new TreeItem(item, SWT.NONE);
							bytes.setText("u1 bytes[length] : " + new String(constantPoolEntry.getUtf8Value()));
							break;
						default:
							throw new NotImplementedException();
					}
					item.setText(entry + " " + CONSTANT_POOL_TAGS.get(e));
				}
			}

			if (!access_flags.isDisposed()) {
				access_flags.removeAll();
				access_flags.setText("Access Flags : 0x" + Integer.toHexString(reader.getAccessFlags()));

				addAccessFlags(access_flags, reader.getAccessFlags(), 2);
			}

			if (!this_class.isDisposed()) {
				int index = reader.getClassIndex();
				IConstantPoolEntry cpe = reader.getConstantPool().decodeEntry(index);

				String className = new String(cpe.getClassInfoName());
				this_class.setText("This Class : " + className);
			}

			if (!super_class.isDisposed()) {
				int index = reader.getSuperclassIndex();
				if (index == 0) {
					super_class.setText("Super Class : java/lang/Object");
				} else {
					IConstantPoolEntry cpe = reader.getConstantPool().decodeEntry(index);

					String className = new String(cpe.getClassInfoName());
					super_class.setText("Super Class : " + className);
				}
			}

			if (!interfaces.isDisposed()) {
				interfaces.removeAll();
				char[][] interfaceNames = reader.getInterfaceNames();
				for (char[] name : interfaceNames) {
					TreeItem item = new TreeItem(interfaces, SWT.NONE);
					item.setText(new String(name));
				}
			}

			if (!fields.isDisposed()) {
				fields.removeAll();
				IFieldInfo[] fieldInfos = reader.getFieldInfos();
				for (IFieldInfo info : fieldInfos) {
					TreeItem fieldInfo = new TreeItem(fields, SWT.NONE);
					fieldInfo.setText(new String(info.getDescriptor()) + " " + new String(info.getName()));

					TreeItem accessFlags = new TreeItem(fieldInfo, SWT.NONE);

					accessFlags.setText("u2 access_flags : 0x" + Integer.toHexString(info.getAccessFlags()));
					addAccessFlags(accessFlags, info.getAccessFlags(), 1);

					TreeItem nameIndex = new TreeItem(fieldInfo, SWT.NONE);
					nameIndex.setText("u2 name_index : " + info.getNameIndex() + " /* " + new String(info.getName()) + " */");

					TreeItem descriptorIndex = new TreeItem(fieldInfo, SWT.NONE);
					descriptorIndex.setText("u2 descriptor_index : " + info.getDescriptorIndex() + " /* " + new String(info.getDescriptor()) + " */");

					TreeItem attributesCount = new TreeItem(fieldInfo, SWT.NONE);
					attributesCount.setText("u2 attributes_count : " + info.getAttributeCount());

					for (int i = 0; i < info.getAttributeCount(); i++) {
						IClassFileAttribute attribute = info.getAttributes()[i];
						if (attribute instanceof SourceFileAttribute) {
							SourceFileAttribute sfa = (SourceFileAttribute) attribute;
							addAttribute(attributesCount, sfa);
						} else {
							throw new NotImplementedException();
						}
					}
				}

			}

			if (!methods.isDisposed()) {
				methods.removeAll();
				IMethodInfo[] methodInfos = reader.getMethodInfos();
				for (IMethodInfo info : methodInfos) {
					TreeItem methodInfo = new TreeItem(methods, SWT.NONE);
					methodInfo.setText(new String(info.getName()) + " " + new String(info.getDescriptor()));

					TreeItem accessFlags = new TreeItem(methodInfo, SWT.NONE);
					accessFlags.setText("u2 access_flags : 0x" + Integer.toHexString(info.getAccessFlags()));

					addAccessFlags(accessFlags, info.getAccessFlags(), 0);

					TreeItem nameIndex = new TreeItem(methodInfo, SWT.NONE);
					nameIndex.setText("u2 name_index : " + info.getNameIndex() + " /* " + new String(info.getName()) + " */");

					TreeItem descriptorIndex = new TreeItem(methodInfo, SWT.NONE);
					descriptorIndex.setText("u2 descriptor_index : " + info.getDescriptorIndex() + " /* " + new String(info.getDescriptor()) + " */");

					TreeItem attributesCount = new TreeItem(methodInfo, SWT.NONE);
					attributesCount.setText("u2 attributes_count : " + info.getAttributeCount());

					for (int i = 0; i < info.getAttributeCount(); i++) {
						IClassFileAttribute attribute = info.getAttributes()[i];
						if (attribute instanceof SourceFileAttribute) {
							SourceFileAttribute sfa = (SourceFileAttribute) attribute;
							addAttribute(attributesCount, sfa);
						} else if (attribute instanceof CodeAttribute) {
							CodeAttribute ca = (CodeAttribute) attribute;
							addAttribute(attributesCount, ca);
						} else {
							throw new NotImplementedException();
						}

					}
				}
			}

			if (!attributes.isDisposed()) {
				attributes.removeAll();
				IClassFileAttribute[] attribs = reader.getAttributes();
				for (IClassFileAttribute attr : attribs) {
					if (attr instanceof SourceFileAttribute) {
						addAttribute(attributes, (SourceFileAttribute) attr);
					} else {
						throw new NotImplementedException();
					}
				}
			}
		}
	}

	private void addAttribute(TreeItem parent, LineNumberAttribute attr) {
		TreeItem header = new TreeItem(parent, SWT.NONE);
		header.setText("LineNumberTable Attribute");

		TreeItem attributeNameIndex = new TreeItem(header, SWT.NONE);
		attributeNameIndex
		.setText("u2 attribute_name_index : " + attr.getAttributeNameIndex() + " /* " + new String(attr.getAttributeName()) + " */");

		TreeItem attributeLength = new TreeItem(header, SWT.NONE);
		attributeLength.setText("u4 attribute_length : " + attr.getAttributeLength());

		TreeItem lineNumberTableLength = new TreeItem(header, SWT.NONE);
		lineNumberTableLength.setText("u2 line_number_table_length : " + attr.getLineNumberTableLength());

		int[][] lineNumbers = attr.getLineNumberTable();

		for (int i = 0; i < attr.getLineNumberTableLength(); i++) {
			TreeItem start_pc = new TreeItem(header, SWT.NONE);
			start_pc.setText("u2 start_pc : " + lineNumbers[i][0]);

			TreeItem line_number = new TreeItem(header, SWT.NONE);
			line_number.setText("u2 line_number : " + lineNumbers[i][1]);
		}

	}

	private void addAttribute(TreeItem parent, CodeAttribute attr) {
		TreeItem header = new TreeItem(parent, SWT.NONE);
		header.setText("Code Attribute");

		TreeItem attributeNameIndex = new TreeItem(header, SWT.NONE);
		attributeNameIndex
		.setText("u2 attribute_name_index : " + attr.getAttributeNameIndex() + " /* " + new String(attr.getAttributeName()) + " */");

		TreeItem attributeLength = new TreeItem(header, SWT.NONE);
		attributeLength.setText("u4 attribute_length : " + attr.getAttributeLength());

		TreeItem maxStack = new TreeItem(header, SWT.NONE);
		maxStack.setText("u2 max_stack : " + attr.getMaxStack());

		TreeItem maxLocals = new TreeItem(header, SWT.NONE);
		maxLocals.setText("u2 max_locals : " + attr.getMaxLocals());

		TreeItem codeLength = new TreeItem(header, SWT.NONE);
		codeLength.setText("u4 code_length : " + attr.getCodeLength());

		byte[] code = attr.getBytecodes();

		for (byte element : code) {
			TreeItem byteCode = new TreeItem(codeLength, SWT.NONE);

			byteCode.setText((element & 0xFF) + " /* " + OpcodeStringValues.BYTECODE_NAMES[element & 0xFF] + " */");
		}

		TreeItem exceptionLength = new TreeItem(header, SWT.NONE);
		exceptionLength.setText("u2 exception_table_length : " + attr.getExceptionTableLength());

		IExceptionTableEntry[] exTable = attr.getExceptionTable();

		for (int i = 0; i < attr.getExceptionTableLength(); i++) {
			IExceptionTableEntry ete = exTable[i];

			TreeItem exTableItem = new TreeItem(exceptionLength, SWT.NONE);
			exTableItem.setText(ete.getStartPC() + " - " + ete.getEndPC() + " " + new String(ete.getCatchType()));

			TreeItem start_pc = new TreeItem(exTableItem, SWT.NONE);
			start_pc.setText("u2 start_pc : " + ete.getStartPC());

			TreeItem end_pc = new TreeItem(exTableItem, SWT.NONE);
			end_pc.setText("u2 end_pc : " + ete.getEndPC());

			TreeItem handler_pc = new TreeItem(exTableItem, SWT.NONE);
			end_pc.setText("u2 handler_pc : " + ete.getHandlerPC());

			TreeItem catch_type = new TreeItem(exTableItem, SWT.NONE);
			end_pc.setText("u2 catch_type : " + ete.getCatchTypeIndex() + " /* " + new String(ete.getCatchType()) + " */");
		}

		TreeItem attributes_count = new TreeItem(header, SWT.NONE);
		attributes_count.setText("u2 attributes count : " + attr.getAttributesCount());

		for (int i = 0; i < attr.getAttributesCount(); i++) {
			IClassFileAttribute attribute = attr.getAttributes()[i];
			if (attribute instanceof SourceFileAttribute) {
				SourceFileAttribute sfa = (SourceFileAttribute) attribute;
				addAttribute(attributes_count, sfa);
			} else if (attribute instanceof CodeAttribute) {
				CodeAttribute ca = (CodeAttribute) attribute;
				addAttribute(attributes_count, ca);
			} else if (attribute instanceof LineNumberAttribute) {
				LineNumberAttribute lna = (LineNumberAttribute) attribute;
				addAttribute(attributes_count, lna);
			} else if (attribute instanceof LocalVariableAttribute) {
				LocalVariableAttribute lva = (LocalVariableAttribute) attribute;
				addAttribute(attributes_count, lva);
			} else {
				throw new NotImplementedException();
			}
		}

	}

	private void addAccessFlags(TreeItem parent, int accessFlags, int version) {
		/**
		 * Version 0 = Method, 1 = Field, 2 = Class
		 */

		TreeItem acc_public = new TreeItem(parent, SWT.NONE);
		acc_public.setText("ACC_PUBLIC : " + (((accessFlags & 0x01) == 0x01) ? "TRUE" : "FALSE"));

		if (version == 0) { // Field
			TreeItem acc_private = new TreeItem(parent, SWT.NONE);
			acc_private.setText("ACC_PRIVATE : " + (((accessFlags & 0x02) == 0x02) ? "TRUE" : "FALSE"));

			TreeItem acc_protected = new TreeItem(parent, SWT.NONE);
			acc_protected.setText("ACC_PROTECTED : " + (((accessFlags & 0x04) == 0x04) ? "TRUE" : "FALSE"));

			TreeItem acc_static = new TreeItem(parent, SWT.NONE);
			acc_static.setText("ACC_STATIC : " + (((accessFlags & 0x08) == 0x08) ? "TRUE" : "FALSE"));

			TreeItem acc_final = new TreeItem(parent, SWT.NONE);
			acc_final.setText("ACC_FINAL : " + (((accessFlags & 0x10) == 0x10) ? "TRUE" : "FALSE"));

			TreeItem acc_synchronized = new TreeItem(parent, SWT.NONE);
			acc_synchronized.setText("ACC_SYNCHRONIZED : " + (((accessFlags & 0x20) == 0x20) ? "TRUE" : "FALSE"));

			TreeItem acc_native = new TreeItem(parent, SWT.NONE);
			acc_native.setText("ACC_NATIVE : " + (((accessFlags & 0x100) == 0x100) ? "TRUE" : "FALSE"));

			TreeItem acc_abstract = new TreeItem(parent, SWT.NONE);
			acc_abstract.setText("ACC_ABSTRACT : " + (((accessFlags & 0x400) == 0x400) ? "TRUE" : "FALSE"));

			TreeItem acc_strict = new TreeItem(parent, SWT.NONE);
			acc_strict.setText("ACC_STRICT : " + (((accessFlags & 0x800) == 0x800) ? "TRUE" : "FALSE"));
		} else if (version == 1) { // Field
			TreeItem acc_private = new TreeItem(parent, SWT.NONE);
			acc_private.setText("ACC_PRIVATE : " + (((accessFlags & 0x02) == 0x02) ? "TRUE" : "FALSE"));

			TreeItem acc_protected = new TreeItem(parent, SWT.NONE);
			acc_protected.setText("ACC_PROTECTED : " + (((accessFlags & 0x04) == 0x04) ? "TRUE" : "FALSE"));

			TreeItem acc_static = new TreeItem(parent, SWT.NONE);
			acc_static.setText("ACC_STATIC : " + (((accessFlags & 0x08) == 0x08) ? "TRUE" : "FALSE"));

			TreeItem acc_final = new TreeItem(parent, SWT.NONE);
			acc_final.setText("ACC_FINAL : " + (((accessFlags & 0x10) == 0x10) ? "TRUE" : "FALSE"));

			TreeItem acc_volatile = new TreeItem(parent, SWT.NONE);
			acc_volatile.setText("ACC_VOLATILE : " + (((accessFlags & 0x40) == 0x40) ? "TRUE" : "FALSE"));

			TreeItem acc_transient = new TreeItem(parent, SWT.NONE);
			acc_transient.setText("ACC_TRANSIENT : " + (((accessFlags & 0x80) == 0x80) ? "TRUE" : "FALSE"));
		} else if (version == 2) { // CLASS
			TreeItem acc_final = new TreeItem(parent, SWT.NONE);
			acc_final.setText("ACC_FINAL : " + (((accessFlags & 0x10) == 0x10) ? "TRUE" : "FALSE"));

			TreeItem acc_super = new TreeItem(parent, SWT.NONE);
			acc_super.setText("ACC_SUPER : " + (((accessFlags & 0x20) == 0x20) ? "TRUE" : "FALSE"));

			TreeItem acc_interface = new TreeItem(parent, SWT.NONE);
			acc_interface.setText("ACC_INTERFACE : " + (((accessFlags & 0x200) == 0x200) ? "TRUE" : "FALSE"));

			TreeItem acc_abstract = new TreeItem(parent, SWT.NONE);
			acc_abstract.setText("ACC_ABSTRACT : " + (((accessFlags & 0x400) == 0x400) ? "TRUE" : "FALSE"));
		}
	}

	private void addAttribute(TreeItem parent, LocalVariableAttribute attr) {
		TreeItem header = new TreeItem(parent, SWT.NONE);
		header.setText("LocalVariableTable Attribute");

		TreeItem attributeNameIndex = new TreeItem(header, SWT.NONE);
		attributeNameIndex
		.setText("u2 attribute_name_index : " + attr.getAttributeNameIndex() + " /* " + new String(attr.getAttributeName()) + " */");

		TreeItem attributeLength = new TreeItem(header, SWT.NONE);
		attributeLength.setText("u4 attribute_length : " + attr.getAttributeLength());

		TreeItem localVariableTableLength = new TreeItem(header, SWT.NONE);
		localVariableTableLength.setText("u2 local_variable_table_length : " + attr.getLocalVariableTableLength());

		ILocalVariableTableEntry[] lvt = attr.getLocalVariableTable();

		for (int i = 0; i < attr.getLocalVariableTableLength(); i++) {
			TreeItem localVariableTableItem = new TreeItem(localVariableTableLength, SWT.NONE);

			TreeItem start_pc = new TreeItem(localVariableTableItem, SWT.NONE);
			start_pc.setText("u2 start_pc : " + lvt[i].getStartPC());

			TreeItem length = new TreeItem(localVariableTableItem, SWT.NONE);
			length.setText("u2 length : " + lvt[i].getLength());

			TreeItem name_index = new TreeItem(localVariableTableItem, SWT.NONE);
			name_index.setText("u2 name_index : " + lvt[i].getNameIndex());

			TreeItem descriptor_index = new TreeItem(localVariableTableItem, SWT.NONE);
			descriptor_index.setText("u2 descriptor_index : " +lvt[i].getDescriptorIndex());

			TreeItem index = new TreeItem(localVariableTableItem, SWT.NONE);
			index.setText("u2 index : " +lvt[i].getIndex());
		}
	}

	private void addAttribute(TreeItem parent, SourceFileAttribute attr) {
		TreeItem header = new TreeItem(parent, SWT.NONE);
		header.setText("SourceFile Attribute");

		TreeItem attributeNameIndex = new TreeItem(header, SWT.NONE);
		attributeNameIndex
		.setText("u2 attribute_name_index : " + attr.getAttributeNameIndex() + " /* " + new String(attr.getAttributeName()) + " */");

		TreeItem attributeLength = new TreeItem(header, SWT.NONE);
		attributeLength.setText("u4 attribute_length : " + attr.getAttributeLength());

		TreeItem sourcefileIndex = new TreeItem(header, SWT.NONE);
		sourcefileIndex.setText("u2 sourcefile_index : " + attr.getSourceFileIndex() + " /* " + new String(attr.getSourceFileName()) + " */");
	}

	private void addAttribute(TreeItem attributesCount, StackMapTableAttribute smta) {

	}

}
