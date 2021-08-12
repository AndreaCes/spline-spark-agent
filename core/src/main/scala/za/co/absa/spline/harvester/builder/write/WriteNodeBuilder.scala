/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.harvester.builder.write

import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import za.co.absa.commons.lang.OptionImplicits._
import za.co.absa.spline.harvester.ComponentCreatorFactory
import za.co.absa.spline.harvester.ModelConstants.OperationExtras
import za.co.absa.spline.harvester.builder.OperationNodeBuilder
import za.co.absa.spline.harvester.postprocessing.PostProcessor
import za.co.absa.spline.producer.model.v1_1.WriteOperation

class WriteNodeBuilder
(command: WriteCommand)
  (val componentCreatorFactory: ComponentCreatorFactory, postProcessor: PostProcessor)
  extends OperationNodeBuilder {

  override protected type R = WriteOperation
  override val operation: LogicalPlan = command.query

  override def build(): WriteOperation = {
    val Seq(uri) = command.sourceIdentifier.uris
    val wop = WriteOperation(
      outputSource = uri,
      append = command.mode == SaveMode.Append,
      id = operationId,
      name = command.name.asOption,
      childIds = childIds,
      params = Map(command.params.toSeq: _*).asOption,
      extra = Map(
        OperationExtras.DestinationType -> command.sourceIdentifier.format
      ).asOption
    )

    postProcessor.process(wop)
  }
}
