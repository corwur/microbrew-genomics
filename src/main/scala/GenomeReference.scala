import org.apache.spark.SparkContext
import org.bdgenomics.adam.rdd.ADAMContext._

class GenomeReference(val sc: SparkContext, val name:String, val pathName:String) {

  val featureSet = sc.loadFeatures(pathName)




}
