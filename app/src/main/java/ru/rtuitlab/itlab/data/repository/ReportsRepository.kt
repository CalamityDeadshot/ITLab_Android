package ru.rtuitlab.itlab.data.repository

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.remote.api.reports.ReportApi
import ru.rtuitlab.itlab.data.remote.api.reports.models.*
import javax.inject.Inject

class ReportsRepository @Inject constructor(
	private val reportApi: ReportApi,
	private val handler: ResponseHandler
) {

	private val mockReportsDataJson = "[{\"id\":\"6239bc7dabc238ef845bfdf3\",\"assignees\":{\"reporter\":\"a40f4d6e-e14d-4662-aa21-287048d0938a\",\"implementer\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\"},\"date\":\"2022-03-22T12:09:33\",\"text\":\"Test report 2@\\n\\t\\n@Test report 2Test report 2Test report 2Test report 2Test report 2\",\"archived\":false},{\"id\":\"624ab0b8abc238ef845bfdf7\",\"assignees\":{\"reporter\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\",\"implementer\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\"},\"date\":\"2022-04-04T08:47:52\",\"text\":\"Мой отчёт о других@\\n\\t\\n@###\\nText\",\"archived\":false},{\"id\":\"625094235d92a65714fda520\",\"assignees\":{\"reporter\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\",\"implementer\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\"},\"date\":\"2022-04-08T19:59:31\",\"text\":\"Отчёт 3@\\n\\t\\n@Текст отчёта 3\",\"archived\":false},{\"id\":\"6251c4425d92a65714fda522\",\"assignees\":{\"reporter\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\",\"implementer\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\"},\"date\":\"2022-04-09T17:37:06\",\"text\":\"Разнос кода Пьянкова Семёна в проекте ITLab-Android@\\n\\t\\n@### Почему Пьянков Семён чел\\nВ [проекте ITLab-Android](https://manage.rtuitlab.dev/projects/619547d3865542ecdf01b1fa/62415f0d6423666fe533e3a2):\\n- отсутствует архитектура \\n- он сделан не по дизайну\\n- его поддержка не представляется возможной\\n- а ещё он написан на `Java`\\n\\nВзять, к примеру, следующий код:\\n```kotlin\\nif (((1..6).random() == (1..6).random()) == true)\\n    throw RussianRouletteLoseException(\\\"Too bad you lost bud\\\")\\n```\\nИгра в русскую рулетку на работе неприемлима! Требую немедленного удаления данного персонажа из проекта и увольнения его из *специализированной учебно-научной лаборатории технологий мультимедиа института информационных технологий **МИРЭА - Российского технологического университета***\",\"archived\":false},{\"id\":\"6259d0277a161a918a8b5b6a\",\"assignees\":{\"reporter\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\",\"implementer\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\"},\"date\":\"2022-04-15T20:05:59\",\"text\":\"Разнос разноса кода Семёна Пьянкова в проекте ITLab-Android@\\n\\t\\n@### **Нормально всё там** \",\"archived\":false},{\"id\":\"625eed897a161a918a8b5b6b\",\"assignees\":{\"reporter\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\",\"implementer\":\"1b2b9072-cc29-4b35-961c-af05768c5e4e\"},\"date\":\"2022-04-19T17:12:41\",\"text\":\"Капибара чиллит у реки вместе с тупорылым крокодилом@\\n\\t\\n@\\n![image_2022-04-19_201147291.png](https://dev.manage.rtuitlab.dev/api/mfs/download/625eed55209a6a51263a3e92)\\n\",\"archived\":false}]"
	private val mockPricedReportsJson = "[{\"reportId\":\"6251c4425d92a65714fda522\",\"approved\":\"2022-04-11T19:01:45.919Z\",\"approverId\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\",\"count\":100000,\"description\":\"Молодец автор\"},{\"reportId\":\"625094235d92a65714fda520\",\"approved\":\"2022-04-08T20:22:05.044Z\",\"approverId\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\",\"count\":-123,\"description\":\"\"},{\"reportId\":\"6239bc7dabc238ef845bfdf3\",\"approved\":\"2022-04-08T20:51:33.006Z\",\"approverId\":\"94f9d711-bdf2-4065-ad72-b2ec8e4b1def\",\"count\":-123321,\"description\":\"Комментарий к оценённому отчёту\"}]"

	suspend fun fetchReportsAboutUser() = handler {
		Json.decodeFromString<List<ReportDto>>(mockReportsDataJson)
	}

	suspend fun fetchPricedReports() = handler {
		Json.decodeFromString<List<ReportSalary>>(mockPricedReportsJson)
	}

	suspend fun fetchAllReports(sorted_by:String? =null) = handler{
		reportApi.getReports(sorted_by)
	}
	suspend fun fetchReportNew(
		implementerId: String? = null,
		name:String? = null,
		text:String
	) = handler {
		reportApi.createReport(
			implementerId,
			ReportRequest(name, text)
		)

	}
	suspend fun fetchReportNew(
		implementerId: String? = null,
		reportRequest: ReportRequest
	) = handler {
		reportApi.createReport(
			implementerId,
			reportRequest
		)

	}
	suspend fun fetchEmployeeReport(
		employeeId: String,
		begin: String? = null,
		end: String? = null
	) = handler {
		reportApi.getReportsOfEmployee(
			begin,
			end,
			employeeId
		)
	}
	suspend fun fetchReportById(id: String) = handler{
		reportApi.getReport(id)
	}
	suspend fun fetchAllReportsSalaryOfUser(userId:String) = handler{
		reportApi.getListReportSalary(userId)
	}
	suspend fun updateReport(reportId:String,requiredReportSalary: ReportSalaryRequest){
		reportApi.updateReportSalary(reportId,requiredReportSalary)
	}
	suspend fun updateReport(reportId:String,count:Int,description:String){
		reportApi.updateReportSalary(reportId,ReportSalaryRequest(count,description))
	}
}