package com.flw.dolores.entities

object GameValues {
    const val conveyor_Breakdown_Limit: Int = 40
    const val conveyor_Scrap_Limit: Int = 20
    const val conveyor_Repair_Cost: Double = 0.25
    const val conveyor_Damage_With_Maintenance: Int = 2
    const val conveyor_Damage_Without_Maintenance: Int = 5
    const val conveyor_Sale_Factor: Double = 0.85
    const val conveyor_disability_factor: Double = 0.02
    const val working_time: Int = 27000
    val processes: List<String> = listOf("en", "wv", "la", "wk", "ve")
    const val costs_abc_analysis: Int = 10000
    const val costs_abc_zoning: Int = 5000
    const val storage_cost_factor: Double = 0.15
    const val factor_debit_interest: Double = 0.1
    const val factor_credit_interest: Double = 0.02
    const val max_critical_state_duration: Int = 3
    const val min_customer_satisfaction: Double = 0.1
    const val module_order_quantity_costs: Int = 450
    const val module_reorder_level_costs: Int = 200
    const val module_safety_stock_costs: Int = 200
    const val module_look_in_storage_costs: Int = 300
    const val module_status_report_costs: Int = 500
    val valid_contract_types: List<Int> = listOf(0, 1, 2)
    val security_qualifications: List<Int> = listOf(2, 3, 6, 7)
    const val new_employee_cost_indefinite: Int = 500
    const val new_employee_cost_temporary: Int = 200
    const val error_chance_wfp_with_training: Double = 0.01
    const val error_chance_wfp_without_training: Double = 0.05
    const val error_chance_wofp_without_training: Double = 0.1
    const val error_chance_with_qm: Double = 0.05
    const val error_chance_without_qm: Double = 0.1
    const val half_time_factor: Double = 0.6
    const val forklift_training_cost: Int = 2300
    const val qm_training_cost: Int = 2250
    const val securityTrainingCost: Int = 1700
    val technology_cost: List<Int> = listOf(0, 850, 1300, 1600)
    val technology_factor: List<Double> = listOf(0.0, 0.02, 0.05, 0.1)
    const val motivation_base: Double = 0.25
    val work_climate_invest_level: List<Int> = listOf(0, 100, 250, 400, 550)
    val work_climate_factor: List<Double> = listOf(0.7, 0.85, 1.0, 1.05, 1.1)
    val overtimeMotivationBorders: List<Int> = listOf(0, 1, 2, 3)
    val overtimeMotivationFactor: List<Double> = listOf(1.0, 0.9, 0.75, 0.5)
    val temporaryMotivationBorders: List<Double> = listOf(0.1, 0.2, 0.3, 0.4, 0.5)
    val temporaryMotivationFactor: List<Double> = listOf(1.0, 0.95, 0.9, 0.85, 0.8, 0.7)
    val salary: List<Double> = listOf(85.0, 125.0, 125.0, 145.0, 110.0, 150.0, 160.0, 170.0)
    val salary_bonus_border: List<Int> = listOf(0, 5, 10, 15, 20, 25)
    val salary_bonus_factor: List<Double> = listOf(0.75, 0.8, 0.85, 0.88, 0.9, 0.95, 1.0)
    const val compensation_factor: Double = 0.3
    const val motivation_warning: Int = 50
    val job_article_probability: List<Double> = listOf(0.1, 0.3, 0.6, 1.0)
    val job_quantity_probability: List<Double> = listOf(0.15, 0.50, 0.70, 0.85, 1.0)
    val customerSatisfactionLevel: List<Int> = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90)
    val customer_satisfaction_factor: List<Double> = listOf(-1.0, -0.8, -0.6, -0.4, -0.2, 0.0, 0.2, 0.4, 0.8, 1.0)
    const val pallet_increase: Int = 25
    val pallet_errors: List<String> = listOf(
        "Schaden",
        "Falsche Anlieferung",
        "Falsche Auslagerung",
        "Transportschaden bei Entladung",
        "Transportschaden im Lager",
        "Transportschaden in Verladung",
        "-"
    )
    const val history_time: Int = 5
    const val stockCarryingFactor: Double = 0.15
    val loading_equipment_level: List<Int> = listOf(0, 450, 800)
    val loading_equipment_crash_chance: List<Double> = listOf(0.1, 0.05, 0.01)
    val loading_equipment_factor: List<Double> = listOf(0.9, 0.95, 0.99)
    val transport_time: List<Double> = listOf(25.0, 25.0)
    const val usd_costs_per_pallet: Int = 12
    const val pallet_control_time_static_we: Int = 40
    const val pallet_control_time_dynamic_we: Int = 150
    const val pallet_control_time_static_wa: Int = 20
    const val pallet_control_time_dynamic_wa: Int = 100
    const val pallet_unit_time_security_devices: Int = 50
    const val timeTakeUpRelease: Int = 5
    val lift_layer_duration: List<Int> = listOf(0, 5, 10, 15)
    const val error_sum: Double = 160.0
    const val error_damage: Double = 10.0
    const val error_wrong_delivered: Double = 10.0
    const val errorWrongRetrieval: Double = 10.0
    const val error_transport_damage_en: Double = 10.0
    const val error_transport_damage_la: Double = 10.0
    const val error_transport_damage_ve: Double = 10.0
    const val global_crash_factor_with_loading_equipment: Double = 0.3
    const val global_crash_factor_with_security_devices: Double = 0.3
    const val global_crash_factor_employee: Double = 0.4
    const val probability_crash_with_unit_safety_devices: Double = 0.0
    const val probability_crash_without_unit_safety_devices: Double = 0.05
    const val time_crash: Int = 1200
    val order_cancel_cost: List<Double> = listOf(0.25, 0.2, 0.1, 0.05, 0.01)
}