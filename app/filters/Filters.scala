package filters

import play.api.http.{DefaultHttpFilters, EnabledFilters}
import play.filters.cors.CORSFilter

import javax.inject.Inject

class Filters @Inject()(enabledFilters: EnabledFilters, CORSFilter: CORSFilter) extends DefaultHttpFilters(enabledFilters.filters :+ CORSFilter: _*)