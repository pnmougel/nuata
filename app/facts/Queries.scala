package facts

/**
 * Created by nico on 02/10/15.
 * Queries for the fact controller
 */

/**
 * Query representing the creation of a fact
 *
 * @param value Numeric value of the fact
 * @param dimensions List of the dimension ids related to this fact
 * @param ooi Id of the object of interest (OOI) related to this fact
 */
case class FactCreateQuery(
              value: Double,
              dimensions: List[Long],
              ooi: Long)
