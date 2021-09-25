package ru.rtuitlab.itlab.ui.employees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.api.Resource
import ru.rtuitlab.itlab.api.users.models.UserModel
import ru.rtuitlab.itlab.ui.employees.components.EmployeeCard
import ru.rtuitlab.itlab.viewmodels.EmployeesViewModel

@Composable
fun Employees(
	employeesViewModel: EmployeesViewModel,
	navController: NavController
) {
	val usersResource by employeesViewModel.userFlow.collectAsState()

	Column(
		modifier = Modifier
			.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.padding(16.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = stringResource(R.string.employees),
				fontSize = 36.sp
			)
		}
		EmployeeList(usersResource, navController)
	}
}

@Composable
private fun EmployeeList(usersResource: Resource<List<UserModel>>, navController: NavController) {
	usersResource.handle(
		onLoading = {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight(),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
		},
		onError = { msg ->
			Text(text = msg)
		},
		onSuccess = { users ->
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(10.dp),
				contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp)
			) {
				items(users) { user ->
					EmployeeCard(
						user = user,
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								navController.navigate("employee/${user.id}")
							}
					)
				}
			}
		}
	)
}