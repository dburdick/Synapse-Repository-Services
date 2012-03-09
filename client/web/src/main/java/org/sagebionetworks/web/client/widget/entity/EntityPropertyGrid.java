package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Element;

/**
 * A widget that renders entity properties.
 * 
 * @author jmhill
 *
 */
public class EntityPropertyGrid extends LayoutContainer {

	private VerticalPanel vp;

	ListStore<EntityRowModel> gridStore;

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		rebuild();

	}

	/**
	 * Rebuild this component.
	 */
	public void rebuild() {
		// there is nothing to do if we have not been rendered.
		if(!this.isRendered()) return;
		this.clearState();
		this.removeAll();
		// Create the grid
		vp = new VerticalPanel();
		vp.setSpacing(10);

		// Renderer for the label column
		GridCellRenderer<EntityRowModel> labelRenderer = new GridCellRenderer<EntityRowModel>() {

			@Override
			public Object render(EntityRowModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<EntityRowModel> store, Grid<EntityRowModel> grid) {
				String value = model.get(property);

				StringBuilder builder = new StringBuilder();
				builder.append("<div style='font-weight:bold; color:grey; white-space:normal; overflow:hidden; text-overflow:ellipsis;'>");
				builder.append(value);
				builder.append(":</div>");
				Html html = new Html(builder.toString());
				return html;
			}

		};

		// Renderer for the value column
		GridCellRenderer<EntityRowModel> valueRenderer = new GridCellRenderer<EntityRowModel>() {

			@Override
			public Object render(EntityRowModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<EntityRowModel> store, Grid<EntityRowModel> grid) {
				String value = model.get(property);
				if (value == null) {
					value = "";
				}
				StringBuilder builder = new StringBuilder();
				builder.append("<div style='font-weight: normal;color:black; overflow:hidden; text-overflow:ellipsis; width:auto;'>");
				builder.append(value);
				builder.append("</div>");
				Html html = new Html(builder.toString());
//				html.setWidth(50);
			    ToolTipConfig tipsConfig = new ToolTipConfig();  
			    tipsConfig.setTitle(model.getToolTipTitle());  
			    tipsConfig.setText(model.getToolTipBody());
			    tipsConfig.setMouseOffset(new int[] {0, 0});  
			    tipsConfig.setAnchor("left");  
			    tipsConfig.setDismissDelay(0);
			    ToolTip tip = new ToolTip(html, tipsConfig);
				return html;
			}

		};
		// Create a grid
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		int col0Width = 150;
		int col1Width = 200;

		// Label
		ColumnConfig column = new ColumnConfig();
		column.setId(EntityRowModel.LABEL);
		column.setHeader("Label");
		column.setWidth(100);
		column.setRowHeader(false);
		column.setRenderer(labelRenderer);
		column.setAlignment(HorizontalAlignment.RIGHT);
		configs.add(column);
		// Value
		column = new ColumnConfig();
		column.setId(EntityRowModel.VALUE);
		column.setHeader("Value");
//		column.setWidth(col1Width);
		column.setRowHeader(false);
		column.setRenderer(valueRenderer);
		configs.add(column);

		ColumnModel cm = new ColumnModel(configs);

		ContentPanel cp = new ContentPanel();
		cp.setBodyBorder(false);
		// cp.setIcon(Resources.ICONS.table());
		// cp.setHeading("Basic Grid");
		cp.setButtonAlign(HorizontalAlignment.CENTER);
		cp.setLayout(new FitLayout());
		cp.setHeaderVisible(false);
		cp.setSize(350, 700);

		final Grid<EntityRowModel> grid = new Grid<EntityRowModel>(gridStore,cm);
		grid.setAutoExpandColumn(EntityRowModel.VALUE);
		grid.setBorders(true);
		grid.setStripeRows(false);
		grid.setColumnLines(false);
		grid.setColumnReordering(false);
		grid.setHideHeaders(true);
		grid.setTrackMouseOver(true);
		grid.setSelectionModel(null);
		grid.setShadow(false);
		grid.getAriaSupport().setLabelledBy(cp.getHeader().getId() + "-label");
		cp.add(grid);
		vp.add(cp);
		this.add(vp);

	}

	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	public void setRows(List<EntityRow<?>> rows) {
		// Build the store from the rows.
		this.gridStore = GridStoreFactory.createListStore(rows);
		this.rebuild();
	}

}