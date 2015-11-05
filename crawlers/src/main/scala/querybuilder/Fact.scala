package querybuilder

import java.util.Date

/**
 * Created by nico on 20/10/15.
 */
class Fact(val value: Option[Double], val valueLong: Option[Long], val dimensions: List[Dimension], val ooi: OOI, val at: Option[Date] = None) {

}
