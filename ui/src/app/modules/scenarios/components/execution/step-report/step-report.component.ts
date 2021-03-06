import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { StepExecutionReport } from '@model';
import { Subscription } from 'rxjs';
import { EventManagerService } from '@shared';

@Component({
    selector: 'chutney-scenario-step-report',
    templateUrl: './step-report.component.html',
    styleUrls: ['./step-report.component.scss']
})
export class StepReportComponent implements OnInit, OnDestroy {
    @Input() step: StepExecutionReport;
    @Input() id: number;

    stepsCollapsed = true;
    informationCollapsed = true;
    errorsCollapsed = true;
    inputCollapsed = true;

    private expandAllSubscription: Subscription;

    constructor(private eventManager: EventManagerService) { }

    ngOnInit() {
        this.expandAllSubscription = this.eventManager.subscribe('toggleScenarioDetails', (data) => {
            this.inputCollapsed = data.expand;
        });
        this.stepsCollapsed = ('PAUSED' !== this.step.status && 'RUNNING' !== this.step.status && 'FAILURE' !== this.step.status);
    }

    ngOnDestroy() {
        this.eventManager.destroy(this.expandAllSubscription);
    }

    getInformation(): string[] {
        if (this.step !== undefined && this.step.information !== undefined) {
            return this.step.information
        } else {
            return [];
        }
    }

    getErrors(): string[] {
        if (this.step !== undefined && this.step.errors !== undefined) {
            return this.step.errors
        } else {
            return [];
        }
    }

    hasInputs(): boolean {
        let size = 0;
        if (this.step.evaluatedInputs) {
            for (const key of Object.getOwnPropertyNames(this.step.evaluatedInputs)) {
                size++;
            }
        }
        return size > 0;
    }

}
