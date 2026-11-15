/**
 * ECharts 按需注册：只引入看板实际用到的图表与组件，
 * 相比 `import * as echarts from 'echarts'` 可减少约 60% 的图表包体积。
 */
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  BarChart, LineChart, PieChart,
  GridComponent, LegendComponent, TooltipComponent,
  CanvasRenderer
])

export type EChartsInstance = ReturnType<typeof echarts.init>
export { echarts }
