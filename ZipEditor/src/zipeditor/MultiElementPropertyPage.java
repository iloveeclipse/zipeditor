package zipeditor;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public abstract class MultiElementPropertyPage extends PropertyPage {
	protected interface PropertyAccessor {
		Object getPropertyValue(Object object);
	};

	protected class MultiplePropertyAccessor implements PropertyAccessor {
		private PropertyDescriptor[] fDescriptors;
		private String fPropertyName;

		public MultiplePropertyAccessor(Class clazz) {
			try {
				fDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			} catch (Exception e) {
				ZipEditorPlugin.log(e);
			}
		}

		public Object getPropertyValue(Object object) {
			for (int i = 0; i < fDescriptors.length; i++) {
				if (!fDescriptors[i].getName().equals(fPropertyName))
					continue;
				try {
					return fDescriptors[i].getReadMethod().invoke(object, null);
				} catch (Exception e) {
					ZipEditorPlugin.log(e);
				}
			}
			return null;
		}

		public PropertyAccessor getAccessor(String property) {
			fPropertyName = property;
			return this;
		}
		
	};

	private IAdaptable[] fElements;

	protected String nonEqualStringLabel = Messages.getString("MultiElementPropertyPage.0"); //$NON-NLS-1$

	public void setElements(IAdaptable[] elements) {
		fElements = elements;
	}
	
	public void setElement(IAdaptable element) {
		if (element != null)
			fElements = new IAdaptable[] { element };
	}
	
	public IAdaptable[] getElements() {
		return fElements;
	}

	protected Combo createCombo(Composite parent, int width, int hspan) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = hspan;
		data.widthHint = convertWidthInCharsToPixels(width);
		combo.setLayoutData(data);
		combo.add(nonEqualStringLabel);
		return combo;
	}

	protected void setFieldText(Text field, PropertyAccessor accessor) {
		setFieldText(field, accessor, false);
	}

	protected void setFieldText(Text field, PropertyAccessor accessor, boolean summate) {
		Object[] values = new Object[fElements.length];
		Number sum = null;
		for (int i = 0; i < fElements.length; i++) {
			values[i] = accessor.getPropertyValue(fElements[i]);
			if (summate && values[i] instanceof Number) {
				sum = new Long(((Number) values[i]).longValue() + (sum != null ? sum.longValue() : 0));
			}
		}
		final Object unequalValue = new Object();
		Object singularValue = values.length > 0 ? values[0] : null;
		for (int i = 1; i < values.length; i++) {
			singularValue = values[i];
			if (values[i] == values[i - 1] || values[i] != null && values[i].equals(values[i - 1]))
				continue;
			singularValue = unequalValue;
			break;
		}
		if (sum != null) {
			field.setText(formatLong(sum.longValue()));
		} else if (singularValue == unequalValue) {
			field.setText(nonEqualStringLabel);
		} else if (singularValue != null) {
			field.setText(singularValue.toString());
		}
	}

	protected void select(Combo combo, PropertyAccessor accessor) {
		boolean isUnequal = false;
		Object previousValue = fElements.length > 0 ? accessor.getPropertyValue(fElements[0]) : null;
		Object singularValue = null;
		for (int i = 0; i < fElements.length; i++) {
			singularValue = accessor.getPropertyValue(fElements[i]);
			if (singularValue == previousValue || singularValue != null && singularValue.equals(previousValue))
				continue;
			previousValue = singularValue;
			isUnequal = true;
		}
		if (isUnequal) {
			combo.select(0);
		} else if (singularValue != null) {
			int index = combo.indexOf(singularValue.toString());
			combo.select(index);
		}
	}

	protected String formatLong(long value) {
		return ZipLabelProvider.formatLong(value);
	}
}
