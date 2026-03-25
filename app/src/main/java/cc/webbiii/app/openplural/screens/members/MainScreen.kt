package cc.webbiii.app.openplural.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.brighter
import cc.webbiii.app.openplural.helper.LocalStorageViewModel
import cc.webbiii.app.openplural.screens.helper.NavControl

@Composable
fun MembersScreen(navControl: NavControl, viewModel: LocalStorageViewModel = viewModel()) {
    val page = rememberPagerState(pageCount = { 3 }, initialPage = 0)

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            verticalAlignment = Alignment.Top,
            state = page
        ) { page ->
            when(page) {
                0 -> MemberTree(navControl, null, viewModel, false)
                1 -> FronterScreen(navControl, viewModel)
                2 -> MemberTree(navControl, null, viewModel, true)
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.brighter(30))
        ) {
            PageButton(R.drawable.baseline_members_32, R.string.members, 0, page)
            PageButton(R.drawable.baseline_friends_32, R.string.fronters, 1, page)
            PageButton(R.drawable.baseline_group_circle_32, R.string.custom_front, 2, page)
        }
    }
}