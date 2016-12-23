package com.linktargeting.elasticsearch.api

trait search {

  sealed trait Sort

  sealed trait SortOrder
  case object Asc extends SortOrder
  case object Desc extends SortOrder

  sealed trait SortMode
  case object Min extends SortMode
  case object Max extends SortMode
  case object Sum extends SortMode
  case object Avg extends SortMode

  case object ScoreSort extends Sort
  case object DocSort extends Sort

  object FieldSort{
    def apply(name: String, order: SortOrder): FieldSort = new FieldSort(name, Some(order))
    def apply(name: String, mode: SortMode): FieldSort = new FieldSort(name, None, Some(mode))
    def apply(name: String, order: SortOrder, mode: SortMode): FieldSort = new FieldSort(name, Some(order), Some(mode))
  }
  case class FieldSort(name: String, order: Option[SortOrder] = None, mode: Option[SortMode] = None) extends Sort

  case class SearchOptions(from: Option[Int] = None, size: Option[Int] = None, sorters: Seq[Sort] = Seq(DocSort))

}
