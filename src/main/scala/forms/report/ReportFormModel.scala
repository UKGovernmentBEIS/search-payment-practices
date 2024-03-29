/*
 * Copyright (C) 2017  Department for Business, Energy and Industrial Strategy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package forms.report

import dbrows.ContractDetailsRow
import forms.DateRange
import models.ContractDetails
import play.api.libs.json.{Json, OWrites}
import utils.YesNo
import utils.YesNo.{No, Yes}

object ReportConstants {
  /**
    * We need to translate word counts into character counts for the database. This constant
    * defines the average word length (including whitespace)
    */
  val averageWordLength = 7

  val longTerms    = 5000
  val longComment  = 2000
  val shortComment = 500

  val paymentTermsWordCount: Int = longTerms
  val paymentTermsCharCount: Int = paymentTermsWordCount * averageWordLength

  val maxContractPeriodCommentWordCount: Int = shortComment
  val maxContractPeriodCommentCharCount: Int = maxContractPeriodCommentWordCount * averageWordLength

  val paymentTermsCommentWordCount: Int = shortComment
  val paymentTermsCommentCharCount: Int = paymentTermsCommentWordCount * averageWordLength

  val disputeResolutionWordCount: Int = longComment
  val disputeResolutionCharCount: Int = disputeResolutionWordCount * averageWordLength

  val paymentTermsChangedWordCount: Int = shortComment
  val paymentTermsChangedCharCount: Int = paymentTermsChangedWordCount * averageWordLength

  val paymentTermsNotifiedWordCount: Int = shortComment
  val paymentTermsNotifiedCharCount: Int = paymentTermsNotifiedWordCount * averageWordLength

  val paymentCodesWordCount      = 35
  val paymentCodesCharCount: Int = paymentCodesWordCount * averageWordLength
}

case class ConditionalText(yesNo: YesNo, text: Option[String]) {
  def normalize: ConditionalText = this match {
    case ConditionalText(No, _)                          => ConditionalText(No, None)
    case ConditionalText(Yes, Some(t)) if t.trim() == "" => ConditionalText(Yes, None)
    case _                                               => this
  }

  def isDefined: Boolean = yesNo.toBoolean
}

object ConditionalText {
  def apply(o: Option[String]): ConditionalText =
    o.map(s => ConditionalText(Yes, Some(s))).getOrElse(ConditionalText(No, None))

  def apply(s: String): ConditionalText = ConditionalText(Some(s))

  implicit val writes: OWrites[ConditionalText] = Json.writes[ConditionalText]
}

case class PercentageSplit(
  percentWithin30Days: Int,
  percentWithin60Days: Int,
  percentBeyond60Days: Int
) {
  def total: Int = percentWithin30Days + percentWithin60Days + percentBeyond60Days
}

object PercentageSplit {
  implicit val writes: OWrites[PercentageSplit] = Json.writes[PercentageSplit]
}

case class PaymentHistory(
  didMakePayment: Option[YesNo],
  averageDaysToPay: Int,
  percentPaidLaterThanAgreedTerms: Int,
  percentageSplit: PercentageSplit
)

object PaymentHistory {
  implicit val writes: OWrites[PaymentHistory] = Json.writes[PaymentHistory]

  def apply(row: ContractDetailsRow): PaymentHistory =
    PaymentHistory(row.didMakePayment, row.averageDaysToPay, row.percentPaidLaterThanAgreedTerms, PercentageSplit(row.percentInvoicesWithin30Days, row.percentInvoicesWithin60Days, row.percentInvoicesBeyond60Days))
}

case class PaymentTermsChanged(comment: ConditionalText, notified: Option[ConditionalText]) {
  /**
    * If the answer to the comment question is No then remove any answer to the Notified question
    */
  def normalise: PaymentTermsChanged = this match {
    case PaymentTermsChanged(c@ConditionalText(No, _), _) => PaymentTermsChanged(c, None)
    case _                                                => this
  }
}

object PaymentTermsChanged {
  implicit val writes: OWrites[PaymentTermsChanged] = Json.writes[PaymentTermsChanged]
}

case class PaymentTerms(
  shortestPaymentPeriod: Int,
  longestPaymentPeriod: Option[Int],
  terms: String,
  maximumContractPeriod: Int,
  maximumContractPeriodComment: Option[String],
  paymentTermsChanged: PaymentTermsChanged,
  paymentTermsComment: Option[String],
  disputeResolution: String
)

object PaymentTerms {
  def apply(row: ContractDetailsRow): PaymentTerms =
    PaymentTerms(
      row.shortestPaymentPeriod, row.longestPaymentPeriod, row.paymentTerms, row.maximumContractPeriod, row.maximumContractPeriodComment,
      pt(row),
      row.paymentTermsChangedComment,
      row.disputeResolution
    )

  def pt(row: ContractDetailsRow): PaymentTermsChanged = {
    val comment = ConditionalText(row.paymentTermsChangedComment)
    val notified =
      if (comment.yesNo == Yes) Some(ConditionalText(row.paymentTermsChangedNotifiedComment))
      else None

    PaymentTermsChanged(comment, notified)
  }

  implicit val writes: OWrites[PaymentTerms] = Json.writes[PaymentTerms]
}

case class ReportingPeriodFormModel(
  reportDates: DateRange,
  hasQualifyingContracts: YesNo
)

case class ShortFormModel(
  paymentCodes: ConditionalText
)

case class LongFormModel(
  paymentHistory: PaymentHistory,
  paymentTerms: PaymentTerms,
  offerEInvoicing: YesNo,
  offerSupplyChainFinancing: YesNo,
  retentionChargesInPolicy: YesNo,
  retentionChargesInPast: YesNo,
  paymentCodes: ConditionalText
)

object LongFormModel {
  def apply(paymentCodes: ConditionalText, report: ContractDetails): LongFormModel = {
    LongFormModel(
      report.paymentHistory,
      report.paymentTerms,
      report.offerEInvoicing,
      report.offerSupplyChainFinance,
      report.retentionChargesInPolicy,
      report.retentionChargesInPast,
      paymentCodes)
  }

}

case class ReportReviewModel(
  confirmedBy: String,
  confirmed: Boolean
)



