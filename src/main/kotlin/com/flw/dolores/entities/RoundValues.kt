package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class RoundValues(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    var accountBalance: Double = 363708.0,
    var customer_satisfaction: Double = 99.0,
    var pMax: Int = 255,
    var game_state: String = "OK",
    var status_change_round: Int = 1000,

    //player set values
    var unit_security_devices_used: Boolean = true,
    var itCosts: Int = 0,
    var strategy_incoming: Int = 0,
    var strategy_outgoing: Int = 3,
    var strategy_storage: Int = 0,
    var pallet_we_factor: Double = 1.0, //former f_control_we TODO Check valid double not int
    var pallet_wa_factor: Double = 1.0, //former f_control_wa TODO Check valid double not int
    val overtime_process: Array<Int> = Array<Int>(5) { 0 },
    var back_to_basic_storage: Int = 0,
    var back_to_it_level1: Int = 0,
    var back_to_it_level2: Int = 0,
    var abc_analysis_round: Int = 0,
    var abc_zoning_round: Int = 0,
    var storage_factor: Double = 0.5,
    var loading_equipment_level: Int = 0,
    var module_order_quantity: Boolean = false,
    var module_reorder_level: Boolean = false,
    var module_safety_stock: Boolean = false,
    var module_look_in_storage: Boolean = false,
    var module_status_report: Boolean = false,

    // Statistics
    var late_jobs: Int = 0,
    var free_storage: Int = 2736,
    var occ_storage: Int = 380,
    var sales_income: Double = 45683.0,
    var storage_cost: Double = 10218.0,
    var workTimeCost: Double = 1370.0,
    var costs_qualification_measure: Double = 0.0,
    var costs_new_employees: Double = 0.0,
    var work_climate_invest: Int = 0,
    var income_conveyor_sale: Double = 0.0,
    var costs_usd: Double = 0.0, // unit security device
    var costs_abc: Double = 0.0,

    // Cost-Income calculator
    var sales_income_article: Array<Double> = Array(4) { 0.0 },
    var costs_round: Double = 12614.0,
    var income_round: Double = 7378.86,
    var debit_interest_cost: Double = 0.0,
    var credit_interest_income: Double = 7131.0,

    // Capacity calculator
    val capacity_processes: Array<Double> = Array(5) { 0.0 },
    val capacity_overall_processes: Array<Double> = Array(5) { 0.0 },
    var capacity_storage_in: Double = 0.0, //TODO include in normal process capacity
    var capacity_storage_out: Double = 0.0, //TODO include in normal process capacity

    // Conveyor calculator
    val conv_capacity_processes: Array<Double> = Array(5) { 0.0 },
    var conv_capacity_wfp_processes: Array<Double> = Array(5) { 0.0 },
    var conv_capacity_wofp_processes: Array<Double> = Array(5) { 0.0 },
    var conv_count_processes: Array<Int> = arrayOf(1, 0, 3, 0, 1),
    var avg_speed_processes: Array<Double> = Array(5) { 0.0 },
    var current_conv_value: Double = 0.0,
    var repair_duration: Double = 0.0,
    var costs_repair: Double = 0.0,
    var costs_maintenance: Double = 180.0,
    var costs_overhaul: Double = 0.0,
    var costs_new: Double = 0.0,

    // Employee calculator
    var emp_capacity_processes: Array<Double> = Array(5) { 0.0 },
    var emp_capacity_wfp_processes: Array<Double> = Array(5) { 0.0 },
    var emp_capacity_wofp_processes: Array<Double> = Array(5) { 0.0 },
    var avg_error_chance_processes: Array<Double> = Array(5) { 0.0 },
    var emp_count_processes: Array<Int> = arrayOf(2, 4, 3, 2, 1),  //count_processes und employee_count_processes
    var employee_cost: Double = 0.0,
    var avg_motivation: Double = 1.0,

    // Throughput calculator
    var accurate_finished_jobs: Int = 0,
    var late_finished_jobs: Int = 0,
    var accurate_delivered_pallets: Int = 0,
    var late_delivered_pallets: Int = 0,
    var pallet_quantity_per_errors: Array<Int> = Array(5) { 0 },
    var pallets_transported_process: Array<Int> = arrayOf(140, 140, 395, 255, 255),
    var pallets_not_transported_process: Array<Int> = Array(5) { 0 },
    var pallets_transported_la_in: Int = 140,
    var pallets_transported_la_out: Int = 255,
    var not_transported_pallets_la_in: Int = 0,
    var not_transported_pallets_la_out: Int = 0,
    var order_costs_article: Array<Double> = Array(4) { 0.0 },
    var order_fix_costs_article: Array<Double> = Array(4) { 0.0 },
    var current_customerJobs: Int = 0,
    var crash_time_processes: Array<Int> = Array(5) { 0 },

    // Post-throughput calculator
    var service_level: Double = 0.99,
    val workload_employee: Array<Double> = arrayOf(9.0, 47.0, 32.0, 58.0, 21.0),
    val workload_conveyor: Array<Double> = arrayOf(9.0, 0.0, 22.0, 0.0, 21.0),

    var workload_employee_storage_in: Double = 22.0, //TODO include in normal process workload
    var workload_employee_storage_out: Double = 42.0, //TODO include in normal process workload
    var workload_conveyor_storage_in: Double = 17.0, //TODO include in normal process workload
    var workload_conveyor_storage_out: Double = 28.0, //TODO include in normal process workload
    var company_value: Double = 260279.5,
    var stock_value: Double = 68120.0,
    val stock_value_processes: Array<Double> = arrayOf(0.0, 0.0, 68120.0, 0.0, 0.0),
    var current_ordered_pallets: Int = 1490,
    var current_order_costs: Double = 0.0,
    var overall_complaint_percentage: Double = 0.0,
    var overall_complaint_damaged: Double = 0.0,
    var overall_complaint_w_delivered: Double = 0.0,
    var overall_complaint_w_retrieval: Double = 0.0,
    var overall_complaint_w_pallets: Double = 0.0,
    var overall_complaint_e_en: Double = 0.0,
    var overall_complaint_e_la: Double = 0.0,
    var overall_complaint_e_ve: Double = 0.0,
    var overall_complaint_e_transport: Double = 0.0,
) {
    fun copy(): RoundValues {
        return RoundValues(
            accountBalance = this.accountBalance,
            customer_satisfaction = this.customer_satisfaction,
            pMax = this.pMax,
            game_state = this.game_state,
            status_change_round = this.status_change_round,
            unit_security_devices_used = this.unit_security_devices_used,
            itCosts = this.itCosts,
            strategy_incoming = this.strategy_incoming,
            strategy_outgoing = this.strategy_outgoing,
            strategy_storage = this.strategy_storage,
            pallet_we_factor = this.pallet_we_factor,
            pallet_wa_factor = this.pallet_wa_factor,
            overtime_process = this.overtime_process,
            back_to_basic_storage = this.back_to_basic_storage,
            back_to_it_level1 = this.back_to_it_level1,
            back_to_it_level2 = this.back_to_it_level2,
            abc_analysis_round = this.abc_analysis_round,
            abc_zoning_round = this.abc_zoning_round,
            storage_factor = this.storage_factor,
            loading_equipment_level = this.loading_equipment_level,
            module_order_quantity = this.module_order_quantity,
            module_reorder_level = this.module_reorder_level,
            module_safety_stock = this.module_safety_stock,
            module_look_in_storage = this.module_look_in_storage,
            module_status_report = this.module_status_report,
            late_jobs = this.late_jobs,
            free_storage = this.free_storage,
            occ_storage = this.occ_storage,
            sales_income = this.sales_income,
            storage_cost = this.storage_cost,
            workTimeCost = this.workTimeCost,
            costs_qualification_measure = this.costs_qualification_measure,
            costs_new_employees = this.costs_new_employees,
            work_climate_invest = this.work_climate_invest,
            income_conveyor_sale = this.income_conveyor_sale,
            costs_usd = this.costs_usd,
            costs_abc = this.costs_abc,
            sales_income_article = this.sales_income_article,
            costs_round = this.costs_round,
            income_round = this.income_round,
            debit_interest_cost = this.debit_interest_cost,
            credit_interest_income = this.credit_interest_income,
            capacity_processes = this.capacity_processes,
            capacity_overall_processes = this.capacity_overall_processes,
            capacity_storage_in = this.capacity_storage_in,
            capacity_storage_out = this.capacity_storage_out,
            conv_capacity_processes = this.conv_capacity_processes,
            conv_capacity_wfp_processes = this.conv_capacity_wfp_processes,
            conv_capacity_wofp_processes = this.conv_capacity_wofp_processes,
            conv_count_processes = this.conv_count_processes,
            avg_speed_processes = this.avg_speed_processes,
            current_conv_value = this.current_conv_value,
            repair_duration = this.repair_duration,
            costs_repair = this.costs_repair,
            costs_maintenance = this.costs_maintenance,
            costs_overhaul = this.costs_overhaul,
            costs_new = this.costs_new,
            emp_capacity_processes = this.emp_capacity_processes,
            emp_capacity_wfp_processes = this.emp_capacity_wfp_processes,
            emp_capacity_wofp_processes = this.emp_capacity_wofp_processes,
            avg_error_chance_processes = this.avg_error_chance_processes,
            emp_count_processes = this.emp_count_processes,
            employee_cost = this.employee_cost,
            avg_motivation = this.avg_motivation,
            accurate_finished_jobs = this.accurate_finished_jobs,
            late_finished_jobs = this.late_finished_jobs,
            accurate_delivered_pallets = this.accurate_delivered_pallets,
            late_delivered_pallets = this.late_delivered_pallets,
            pallet_quantity_per_errors = this.pallet_quantity_per_errors,
            pallets_transported_process = this.pallets_transported_process,
            pallets_not_transported_process = this.pallets_not_transported_process,
            pallets_transported_la_in = this.pallets_transported_la_in,
            pallets_transported_la_out = this.pallets_transported_la_out,
            not_transported_pallets_la_in = this.not_transported_pallets_la_in,
            not_transported_pallets_la_out = this.not_transported_pallets_la_out,
            order_costs_article = this.order_costs_article,
            order_fix_costs_article = this.order_fix_costs_article,
            current_customerJobs = this.current_customerJobs,
            crash_time_processes = this.crash_time_processes,
            service_level = this.service_level,
            workload_employee = this.workload_employee,
            workload_conveyor = this.workload_conveyor,
            workload_employee_storage_in = this.workload_employee_storage_in,
            workload_employee_storage_out = this.workload_employee_storage_out,
            workload_conveyor_storage_in = this.workload_conveyor_storage_in,
            workload_conveyor_storage_out = this.workload_conveyor_storage_out,
            company_value = this.company_value,
            stock_value = this.stock_value,
            stock_value_processes = this.stock_value_processes,
            current_ordered_pallets = this.current_ordered_pallets,
            current_order_costs = this.current_order_costs,
            overall_complaint_percentage = this.overall_complaint_percentage,
            overall_complaint_damaged = this.overall_complaint_damaged,
            overall_complaint_w_delivered = this.overall_complaint_w_delivered,
            overall_complaint_w_retrieval = this.overall_complaint_w_retrieval,
            overall_complaint_w_pallets = this.overall_complaint_w_pallets,
            overall_complaint_e_en = this.overall_complaint_e_en,
            overall_complaint_e_la = this.overall_complaint_e_la,
            overall_complaint_e_ve = this.overall_complaint_e_ve,
            overall_complaint_e_transport = this.overall_complaint_e_transport,
        )
    }
}