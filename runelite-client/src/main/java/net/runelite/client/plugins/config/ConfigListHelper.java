package net.runelite.client.plugins.config;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

public class ConfigListHelper
{

	static Action addTextfieldToModel(JTextField textField, DefaultListModel<String> model)
	{
		return new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.addElement(textField.getText().replace(",", ""));
				textField.setText("");
			}
		};
	}

	static MouseAdapter rightClickToRemoveAdapter(JList<String> list, DefaultListModel<String> model)
	{
		return new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					list.setSelectedIndex(list.locationToIndex(e.getPoint()));
					JPopupMenu menu = new JPopupMenu();
					JMenuItem remove = new JMenuItem("Remove item");
					menu.add(remove);
					remove.addMouseListener(new MouseAdapter()
					{
						@Override
						public void mousePressed(MouseEvent e)
						{
							if (model.size() > 0)
							{
								model.remove(list.getSelectedIndex());
							}
						}
					});
					menu.show(list, e.getX(), e.getY());
				}
			}
		};
	}

}
